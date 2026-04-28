package heyso.HeysoDiaryBackEnd.ai.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AI 호출 경로의 병목 구간을 측정하기 위한 타이머 어노테이션.
 * {@link AiTimingAspect} 가 메서드 실행 전후 경과 시간을 {@code [AI-TIMING]} 로그로 기록한다.
 *
 * @see AiTimingAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AiTimed {

    /** 로그에 찍힐 도메인 식별자. 예: "diaryAi", "diaryAiPolish", "aichat", "ai_executor", "openai_client" */
    String domain();

    /** 측정 단계 식별자. 예: "total", "ai_call_total", "ai_http_call" */
    String phase();
}
