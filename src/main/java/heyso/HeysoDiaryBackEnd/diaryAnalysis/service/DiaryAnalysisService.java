package heyso.HeysoDiaryBackEnd.diaryAnalysis.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.mapper.DiaryAnalysisMapper;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysis;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisCandidate;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisResult;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.TraitDefinition;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.support.DiaryAnalysisAiClient;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.support.DiaryAnalysisAiClient.ResolvedAnalysisPrompt;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.support.DiaryAnalysisResponseParser;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.type.DiaryAnalysisErrorCode;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.type.DiaryAnalysisException;
import heyso.HeysoDiaryBackEnd.utils.JsonHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryAnalysisService {
    private final DiaryAnalysisMapper diaryAnalysisMapper;
    private final DiaryMapper diaryMapper;
    private final DiaryAnalysisDirtyMarker dirtyMarker;
    private final DiaryAnalysisAiClient aiClient;
    private final DiaryAnalysisResponseParser responseParser;
    private final ObjectMapper objectMapper;
    private final DiaryAnalysisPersistenceService persistenceService;

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
            analysis = persistenceService.createAnalysis(candidate, prompt.bindingResolution());
            String content = aiClient.execute(prompt);
            DiaryAnalysisResult result = responseParser.parse(content, candidate,
                    referenceData.eventTypes(), referenceData.emotions(), referenceData.traitKeys());
            persistenceService.completeSuccess(analysis, result);
            return ProcessResult.SUCCESS;
        } catch (DiaryAnalysisException e) {
            persistenceService.completeFailure(candidate, analysis, e.getErrorCode(), e.getMessage(), null);
            return ProcessResult.FAILED;
        } catch (Exception e) {
            persistenceService.completeFailure(candidate, analysis, DiaryAnalysisErrorCode.INTERNAL_ERROR,
                    "Diary analysis internal error", null);
            throw e;
        }
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
        variables.put("title", StringUtils.defaultString(candidate.getTitle()));
        variables.put("content_md", StringUtils.defaultString(candidate.getContentMd()));
        variables.put("mood_id", StringUtils.defaultString(candidate.getMoodId()));
        variables.put("tags_json", renderPromptVariableJson(tags == null ? List.of() : tags));
        variables.put("trait_definitions_json", renderPromptVariableJson(toPromptTraitKeys(referenceData.traits())));
        variables.put("event_type_codes_json", renderPromptVariableJson(referenceData.eventTypes()));
        variables.put("emotion_codes_json", renderPromptVariableJson(referenceData.emotions()));
        return variables;
    }

    private List<String> toPromptTraitKeys(List<TraitDefinition> traits) {
        return traits.stream()
                .map(TraitDefinition::getTraitKey)
                .toList();
    }

    /*
     * Performance-test rollback note:
     * Keep this richer prompt payload disabled while testing the traitKey-only input.
     */
    // private List<PromptTraitDefinition> toPromptTraits(List<TraitDefinition> traits) {
    //     return traits.stream()
    //             .map(trait -> new PromptTraitDefinition(
    //                     trait.getTraitKey(),
    //                     trait.getTraitName(),
    //                     trait.getTraitDescription()))
    //             .toList();
    // }

    private String renderPromptVariableJson(Object value) {
        return JsonHashUtil.toJson(objectMapper, value,
                e -> new DiaryAnalysisException(DiaryAnalysisErrorCode.INTERNAL_ERROR,
                        "Failed to render diary analysis prompt variables", e));
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

    // private record PromptTraitDefinition(
    //         String traitKey,
    //         String traitName,
    //         String traitDescription) {
    // }
}
