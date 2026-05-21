package heyso.HeysoDiaryBackEnd.diaryAnalysisMng.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisDiaryRow;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisRow;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryAnalysisSearchRequest;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryEventRow;
import heyso.HeysoDiaryBackEnd.diaryAnalysisMng.dto.AdminDiaryTraitEvidenceRow;

@Mapper
public interface AdminDiaryAnalysisMapper {
    List<AdminDiaryAnalysisDiaryRow> selectDiaryAnalysisPage(@Param("request") AdminDiaryAnalysisSearchRequest request);

    long countDiaryAnalysisPage(@Param("request") AdminDiaryAnalysisSearchRequest request);

    AdminDiaryAnalysisDiaryRow selectDiaryAnalysisDetail(@Param("diaryId") Long diaryId);

    List<AdminDiaryEventRow> selectActiveEvents(@Param("diaryId") Long diaryId);

    List<AdminDiaryTraitEvidenceRow> selectActiveTraitEvidence(@Param("diaryId") Long diaryId);

    List<AdminDiaryAnalysisRow> selectAnalyses(@Param("diaryId") Long diaryId);

    int markReanalysisEligible(@Param("diaryId") Long diaryId);
}
