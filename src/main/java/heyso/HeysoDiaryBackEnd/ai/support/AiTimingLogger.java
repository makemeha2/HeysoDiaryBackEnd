package heyso.HeysoDiaryBackEnd.ai.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * AI API 호출 흐름의 단계(phase)별 경과 시간을 INFO 로그로 남긴다.
 *
 * <pre>
 * 사용 예:
 *   long t = AiTimingLogger.start();
 *   // ... 작업 수행
 *   AiTimingLogger.end("diaryAi", "ai_http_call", t,
 *       "provider", "OPENAI", "model", "gpt-4o-mini", "outcome", "success");
 *
 * 로그 포맷 (grep 용이성):
 *   [AI-TIMING] domain=diaryAi phase=ai_http_call elapsedMs=12034 traceId=abc123 provider=OPENAI ...
 * </pre>
 *
 * 운영에서 OpenAI API 호출 지연/타임아웃 병목 식별을 위해 도입.
 * payload(메시지 본문 등 PII)는 절대 로깅하지 않는다.
 */
public final class AiTimingLogger {

    private static final Logger log = LoggerFactory.getLogger("AI_TIMING");
    private static final String PREFIX = "[AI-TIMING]";

    private AiTimingLogger() {
    }

    /** 측정 시작 시점의 nanoTime 반환. */
    public static long start() {
        return System.nanoTime();
    }

    /**
     * 단계 경과 시간을 INFO 로 기록한다.
     *
     * @param domain    호출 도메인 (diaryAi / diaryAiPolish / aichat / ai_executor / openai_client 등)
     * @param phase     단계명 (pre / ai / post / total / ai_call_total / ai_http_call / ai_response_parse 등)
     * @param startNano {@link #start()} 반환값
     * @param kv        가변 key/value 쌍 (provider, model, outcome 등). null/빈 값은 자동 무시.
     */
    public static void end(String domain, String phase, long startNano, Object... kv) {
        long elapsedMs = (System.nanoTime() - startNano) / 1_000_000L;
        StringBuilder sb = new StringBuilder(128);
        sb.append(PREFIX)
                .append(" domain=").append(domain)
                .append(" phase=").append(phase)
                .append(" elapsedMs=").append(elapsedMs)
                .append(" traceId=").append(resolveTraceId());

        if (kv != null) {
            for (int i = 0; i + 1 < kv.length; i += 2) {
                Object key = kv[i];
                Object value = kv[i + 1];
                if (key == null || value == null) {
                    continue;
                }
                String valueStr = String.valueOf(value);
                if (valueStr.isEmpty()) {
                    continue;
                }
                sb.append(' ').append(key).append('=').append(valueStr);
            }
        }

        log.info(sb.toString());
    }

    private static String resolveTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isBlank()) {
            traceId = MDC.get("trace_id");
        }
        return traceId == null || traceId.isBlank() ? "-" : traceId;
    }
}
