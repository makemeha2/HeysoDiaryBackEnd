package heyso.HeysoDiaryBackEnd.diaryAnalysis.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.mapper.DiaryAnalysisMapper;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysis;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisCandidate;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisResult;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryEvent;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryTraitEvidence;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryAnalysisPersistenceService {
    private final DiaryAnalysisMapper diaryAnalysisMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DiaryAnalysis createAnalysis(DiaryAnalysisCandidate candidate,
            AiPromptResolver.BindingResolution bindingResolution) {
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
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeSuccess(DiaryAnalysis analysis, DiaryAnalysisResult result) {
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
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeFailure(DiaryAnalysisCandidate candidate, DiaryAnalysis analysis,
            String errorCode, String errorMessage, String rawResponseJson) {
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
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
