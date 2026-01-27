package heyso.HeysoDiaryBackEnd.diaryAi.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiComment;
import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiFeedback;
import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiRun;
import heyso.HeysoDiaryBackEnd.diaryAi.model.DiaryAiRunContext;

@Mapper
public interface DiaryAiMapper {

    /* ============================== tb_diary_ai_run ============================== */

    void insertDiaryAiRun(DiaryAiRun run);

    int updateDiaryAiRunSuccess(@Param("runId") Long runId,
                                @Param("requestId") String requestId,
                                @Param("promptTokens") Integer promptTokens,
                                @Param("completionTokens") Integer completionTokens,
                                @Param("totalTokens") Integer totalTokens,
                                @Param("costUsd") BigDecimal costUsd);

    int updateDiaryAiRunError(@Param("runId") Long runId,
                              @Param("errorCode") String errorCode,
                              @Param("errorMessage") String errorMessage);

    DiaryAiRun selectDiaryAiRunById(@Param("runId") Long runId);

    DiaryAiRun selectLatestDiaryAiRunByDiaryId(@Param("diaryId") Long diaryId,
                                               @Param("userId") Long userId);

    /* ========================== tb_diary_ai_run_context ========================== */

    int insertDiaryAiRunContextList(@Param("runId") Long runId,
                                    @Param("contexts") List<DiaryAiRunContext> contexts);

    List<DiaryAiRunContext> selectDiaryAiRunContexts(@Param("runId") Long runId);

    /* ============================= tb_diary_ai_comment =========================== */

    void insertDiaryAiComment(DiaryAiComment comment);

    DiaryAiComment selectAiCommentById(@Param("aiCommentId") Long aiCommentId);

    List<DiaryAiComment> selectAiCommentsByDiaryId(@Param("diaryId") Long diaryId,
                                                   @Param("userId") Long userId,
                                                   @Param("limit") Integer limit);

    int updatePinAllOff(@Param("diaryId") Long diaryId,
                        @Param("userId") Long userId);

    int updatePinOn(@Param("aiCommentId") Long aiCommentId,
                    @Param("userId") Long userId);

    /* ============================= tb_diary_ai_feedback ========================== */

    int insertDiaryAiFeedback(DiaryAiFeedback feedback);

    DiaryAiFeedback selectFeedbackByCommentAndUser(@Param("aiCommentId") Long aiCommentId,
                                                   @Param("userId") Long userId);
}
