package heyso.HeysoDiaryBackEnd.diaryAi.support;

import heyso.HeysoDiaryBackEnd.mypage.model.UserAIFeedbackSetting;
import heyso.HeysoDiaryBackEnd.mypage.model.UserProfile;

/**
 * DiaryAiClient 에 전달하는 AI 호출 입력.
 *
 * <pre>
 * 프롬프트 조립에 필요한 두 가지 정보를 묶어 전달한다.
 *   1. 일기 데이터  - 오늘 일기 내용 + 과거 일기 컨텍스트 블록
 *   2. 사용자 설정  - 프로필(닉네임·MBTI) + AI 피드백 설정(말투·스타일 등)
 * </pre>
 *
 * @param diaryDate      오늘 일기 날짜 (yyyy-MM-dd 문자열)
 * @param diaryTitle     오늘 일기 제목
 * @param diaryContent   오늘 일기 본문 (최대 길이 잘린 스니펫)
 * @param contextBlock   과거 일기 컨텍스트 블록 (시스템 프롬프트 {{context_block}} 자리에 주입)
 * @param userProfile    사용자 프로필. null 이면 DiaryAiClient 가 기본값으로 처리
 * @param feedbackSetting 사용자 AI 피드백 설정. null 이면 DiaryAiClient 가 기본값으로 처리
 */
public record DiaryAiCallInput(
        String diaryDate,
        String diaryTitle,
        String diaryContent,
        String contextBlock,
        UserProfile userProfile,
        UserAIFeedbackSetting feedbackSetting) {
}
