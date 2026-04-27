package heyso.HeysoDiaryBackEnd.diaryAi.support;

import heyso.HeysoDiaryBackEnd.ai.support.AiModelResolver;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;

/**
 * AI 호출 1단계(프롬프트 해석) 결과.
 *
 * <pre>
 * DiaryAiClient.resolve() 가 반환하며, 서비스 레이어가 이 값을 이용해
 * tb_diary_ai_run 실행 기록을 먼저 저장한 뒤 2단계(AI 호출)에 재사용한다.
 *
 * 두 단계로 나눈 이유:
 *   - 실행 기록(tb_diary_ai_run)에는 AI 호출 전 확정된 프롬프트·모델이 필요하다.
 *   - AI 호출이 실패해도 어떤 프롬프트로 시도했는지 감사 추적이 가능해야 한다.
 * </pre>
 *
 * @param renderedSystemPrompt 변수 치환이 완료된 시스템 프롬프트 (tb_diary_ai_run.prompt_system 저장용)
 * @param renderedUserPrompt   변수 치환이 완료된 유저 프롬프트 (tb_diary_ai_run.prompt_user 저장용)
 * @param profile              런타임 프로파일 (temperature · topP · maxTokens 결정에 사용)
 * @param resolvedModel        해석된 모델 정보 (provider · 실제 모델명)
 */
public record DiaryAiResolution(
        String renderedSystemPrompt,
        String renderedUserPrompt,
        AiRuntimeProfile profile,
        AiModelResolver.ResolvedModel resolvedModel) {
}
