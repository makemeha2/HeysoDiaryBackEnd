package heyso.HeysoDiaryBackEnd.diaryAnalysis.service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.mapper.DiaryAnalysisMapper;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysis;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisCandidate;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisErrorCode;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisResult;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryEvent;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryTraitEvidence;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.TraitDefinition;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.service.DiaryAnalysisAiClient.ResolvedAnalysisPrompt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryAnalysisService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final DiaryAnalysisMapper diaryAnalysisMapper;
    private final DiaryMapper diaryMapper;
    private final DiaryAnalysisDirtyMarker dirtyMarker;
    private final DiaryAnalysisAiClient aiClient;
    private final DiaryAnalysisResponseParser responseParser;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.diary.analysis.batch-size:20}")
    private int batchSize;

    public AdminBatchRunResult analyzePendingDiaries() {
        int limit = Math.max(1, batchSize);
        List<DiaryAnalysisCandidate> candidates = diaryAnalysisMapper.selectPendingAnalysisCandidates(limit);
        if (candidates.isEmpty()) {
            return new AdminBatchRunResult(0, 0, "분석 대상 일기가 없습니다.");
        }

        AnalysisReferenceData referenceData = loadReferenceData();
        int successCount = 0;
        int failureCount = 0;
        int skippedCount = 0;
        log.info("Diary analysis batch started. candidateCount={}, batchSize={}", candidates.size(), limit);
        for (DiaryAnalysisCandidate candidate : candidates) {
            try {
                ProcessResult result = analyzeOne(candidate, referenceData);
                if (result == ProcessResult.SUCCESS) {
                    successCount++;
                } else if (result == ProcessResult.FAILED) {
                    failureCount++;
                } else if (result == ProcessResult.SKIPPED) {
                    skippedCount++;
                }
            } catch (Exception e) {
                failureCount++;
                log.error("Diary analysis failed. diaryId={}, userId={}",
                        candidate.getDiaryId(), candidate.getUserId(), e);
            }
        }
        log.info("Diary analysis batch finished. candidateCount={}, successCount={}, failureCount={}, skippedCount={}",
                candidates.size(), successCount, failureCount, skippedCount);
        String message = "처리 후보 " + candidates.size() + "건 중 성공 " + successCount
                + "건, 실패 " + failureCount + "건, 보류 " + skippedCount + "건";
        return new AdminBatchRunResult(successCount, failureCount, message);
    }

    private ProcessResult analyzeOne(DiaryAnalysisCandidate candidate, AnalysisReferenceData referenceData) {
        List<String> tags = diaryMapper.selectTagNamesByDiaryId(candidate.getDiaryId());
        String currentHash = dirtyMarker.buildContentHash(candidate.getTitle(), candidate.getContentMd(),
                candidate.getDiaryDate(), candidate.getMoodId(), tags);
        if (!StringUtils.equals(candidate.getContentHash(), currentHash)) {
            diaryAnalysisMapper.restoreDirtyAfterContentChanged(candidate.getDiaryId(), currentHash);
            return ProcessResult.SKIPPED;
        }

        int claimed = diaryAnalysisMapper.claimDiaryAnalysis(candidate.getDiaryId(), candidate.getContentHash());
        if (claimed == 0) {
            return ProcessResult.SKIPPED;
        }

        DiaryAnalysis analysis = null;
        try {
            ResolvedAnalysisPrompt prompt = aiClient.resolve(buildVariables(candidate, tags, referenceData));
            analysis = createAnalysis(candidate, prompt.bindingResolution());
            String content = aiClient.execute(prompt);
            DiaryAnalysisResult result = responseParser.parse(content, candidate,
                    referenceData.eventTypes(), referenceData.emotions(), referenceData.traitKeys());
            completeSuccess(analysis, result);
            return ProcessResult.SUCCESS;
        } catch (DiaryAnalysisException e) {
            completeFailure(candidate, analysis, e.getErrorCode(), e.getMessage(), null);
            return ProcessResult.FAILED;
        } catch (Exception e) {
            completeFailure(candidate, analysis, DiaryAnalysisErrorCode.INTERNAL_ERROR,
                    "Diary analysis internal error", null);
            throw e;
        }
    }

    private DiaryAnalysis createAnalysis(DiaryAnalysisCandidate candidate,
            AiPromptResolver.BindingResolution bindingResolution) {
        return transactionTemplate.execute(status -> {
            DiaryAnalysis analysis = new DiaryAnalysis();
            analysis.setDiaryId(candidate.getDiaryId());
            analysis.setUserId(candidate.getUserId());
            analysis.setAnalysisVersion(diaryAnalysisMapper.selectNextAnalysisVersion(candidate.getDiaryId()));
            analysis.setContentHash(candidate.getContentHash());
            analysis.setDiaryUpdatedAtSnapshot(candidate.getDiaryUpdatedAt());
            analysis.setBindingId(bindingResolution.binding().getBindingId());
            analysis.setSystemTemplateId(bindingResolution.binding().getSystemTemplateId());
            analysis.setUserTemplateId(bindingResolution.binding().getUserTemplateId());
            analysis.setRuntimeProfileId(bindingResolution.binding().getRuntimeProfileId());
            diaryAnalysisMapper.insertDiaryAnalysis(analysis);
            return analysis;
        });
    }

    private void completeSuccess(DiaryAnalysis analysis, DiaryAnalysisResult result) {
        transactionTemplate.executeWithoutResult(status -> {
            for (DiaryEvent event : result.events()) {
                event.setAnalysisId(analysis.getAnalysisId());
            }
            for (DiaryTraitEvidence evidence : result.traitEvidence()) {
                evidence.setAnalysisId(analysis.getAnalysisId());
            }

            diaryAnalysisMapper.deactivateActiveAnalysis(analysis.getDiaryId(), analysis.getAnalysisId());
            diaryAnalysisMapper.deactivateActiveEvents(analysis.getDiaryId());
            diaryAnalysisMapper.deactivateActiveTraitEvidence(analysis.getDiaryId());
            if (!result.events().isEmpty()) {
                diaryAnalysisMapper.insertDiaryEvents(result.events());
            }
            if (!result.traitEvidence().isEmpty()) {
                diaryAnalysisMapper.insertDiaryTraitEvidence(result.traitEvidence());
            }
            analysis.setRawResponseJson(result.rawResponseJson());
            analysis.setSummaryText(result.summary());
            diaryAnalysisMapper.updateDiaryAnalysisSuccess(analysis);
            diaryAnalysisMapper.updateStateSuccess(analysis.getDiaryId(), analysis.getAnalysisId());
        });
    }

    private void completeFailure(DiaryAnalysisCandidate candidate, DiaryAnalysis analysis,
            String errorCode, String errorMessage, String rawResponseJson) {
        transactionTemplate.executeWithoutResult(status -> {
            DiaryAnalysis failedAnalysis = analysis;
            if (failedAnalysis == null) {
                failedAnalysis = new DiaryAnalysis();
                failedAnalysis.setDiaryId(candidate.getDiaryId());
                failedAnalysis.setUserId(candidate.getUserId());
                failedAnalysis.setAnalysisVersion(diaryAnalysisMapper.selectNextAnalysisVersion(candidate.getDiaryId()));
                failedAnalysis.setContentHash(candidate.getContentHash());
                failedAnalysis.setDiaryUpdatedAtSnapshot(candidate.getDiaryUpdatedAt());
                diaryAnalysisMapper.insertDiaryAnalysis(failedAnalysis);
            }
            failedAnalysis.setRawResponseJson(rawResponseJson);
            failedAnalysis.setErrorCode(errorCode);
            failedAnalysis.setErrorMessage(truncate(errorMessage, 1000));
            diaryAnalysisMapper.updateDiaryAnalysisFailed(failedAnalysis);
            diaryAnalysisMapper.updateStateFailed(candidate.getDiaryId(), errorCode, truncate(errorMessage, 1000));
        });
    }

    private AnalysisReferenceData loadReferenceData() {
        List<TraitDefinition> traits = diaryAnalysisMapper.selectActiveCoreTraitDefinitions();
        Set<String> traitKeys = traits.stream()
                .map(TraitDefinition::getTraitKey)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> eventTypes = Set.copyOf(diaryAnalysisMapper.selectActiveCommonCodeIds("DIARY_EVENT_TYPE"));
        Set<String> emotions = Set.copyOf(diaryAnalysisMapper.selectActiveCommonCodeIds("DIARY_EMOTION"));
        return new AnalysisReferenceData(traits, traitKeys, eventTypes, emotions);
    }

    private Map<String, String> buildVariables(DiaryAnalysisCandidate candidate, List<String> tags,
            AnalysisReferenceData referenceData) {
        Map<String, String> variables = new HashMap<>();
        variables.put("diary_id", String.valueOf(candidate.getDiaryId()));
        variables.put("user_id", String.valueOf(candidate.getUserId()));
        variables.put("diary_date", candidate.getDiaryDate() == null ? "" : DATE_FORMATTER.format(candidate.getDiaryDate()));
        variables.put("title", StringUtils.defaultString(candidate.getTitle()));
        variables.put("content_md", StringUtils.defaultString(candidate.getContentMd()));
        variables.put("mood_id", StringUtils.defaultString(candidate.getMoodId()));
        variables.put("diary_updated_at", candidate.getDiaryUpdatedAt() == null ? ""
                : DATE_TIME_FORMATTER.format(candidate.getDiaryUpdatedAt()));
        variables.put("tags_json", toJson(tags == null ? List.of() : tags));
        variables.put("trait_definitions_json", toJson(referenceData.traits()));
        variables.put("event_type_codes_json", toJson(referenceData.eventTypes()));
        variables.put("emotion_codes_json", toJson(referenceData.emotions()));
        return variables;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new DiaryAnalysisException(DiaryAnalysisErrorCode.INTERNAL_ERROR,
                    "Failed to render diary analysis prompt variables", e);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private enum ProcessResult {
        SUCCESS,
        FAILED,
        SKIPPED
    }

    private record AnalysisReferenceData(
            List<TraitDefinition> traits,
            Set<String> traitKeys,
            Set<String> eventTypes,
            Set<String> emotions) {
    }
}
