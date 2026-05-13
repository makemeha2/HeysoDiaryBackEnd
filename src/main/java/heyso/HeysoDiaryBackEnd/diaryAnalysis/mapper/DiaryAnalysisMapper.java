package heyso.HeysoDiaryBackEnd.diaryAnalysis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysis;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisCandidate;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryEvent;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryTraitEvidence;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.TraitDefinition;

@Mapper
public interface DiaryAnalysisMapper {
    void markDiaryAnalysisDirty(@Param("diaryId") Long diaryId,
            @Param("userId") Long userId,
            @Param("contentHash") String contentHash);

    int markDiaryAnalysisStale(@Param("diaryId") Long diaryId,
            @Param("userId") Long userId);

    List<DiaryAnalysisCandidate> selectPendingAnalysisCandidates(@Param("limit") int limit);

    int claimDiaryAnalysis(@Param("diaryId") Long diaryId,
            @Param("contentHash") String contentHash);

    int restoreDirtyAfterContentChanged(@Param("diaryId") Long diaryId,
            @Param("contentHash") String contentHash);

    int selectNextAnalysisVersion(@Param("diaryId") Long diaryId);

    void insertDiaryAnalysis(DiaryAnalysis analysis);

    void updateDiaryAnalysisSuccess(DiaryAnalysis analysis);

    void updateDiaryAnalysisFailed(DiaryAnalysis analysis);

    void deactivateActiveAnalysis(@Param("diaryId") Long diaryId,
            @Param("analysisId") Long analysisId);

    void deactivateActiveEvents(@Param("diaryId") Long diaryId);

    void deactivateActiveTraitEvidence(@Param("diaryId") Long diaryId);

    void insertDiaryEvents(@Param("events") List<DiaryEvent> events);

    void insertDiaryTraitEvidence(@Param("evidenceList") List<DiaryTraitEvidence> evidenceList);

    void updateStateSuccess(@Param("diaryId") Long diaryId,
            @Param("analysisId") Long analysisId);

    void updateStateFailed(@Param("diaryId") Long diaryId,
            @Param("errorCode") String errorCode,
            @Param("errorMessage") String errorMessage);

    List<TraitDefinition> selectActiveCoreTraitDefinitions();

    List<String> selectActiveCommonCodeIds(@Param("groupId") String groupId);
}
