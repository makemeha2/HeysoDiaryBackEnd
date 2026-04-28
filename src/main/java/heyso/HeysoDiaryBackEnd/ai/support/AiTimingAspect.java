package heyso.HeysoDiaryBackEnd.ai.support;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * {@link AiTimed} 가 붙은 메서드의 실행 시간을 {@link AiTimingLogger} 로 기록하는 AOP Aspect.
 *
 * 측정 계층 (요청 1건당 출력 순서 예시):
 * <pre>
 *   domain=diaryAi         phase=total           ← 도메인 서비스 진입 전체
 *   domain=ai_executor     phase=ai_call_total   ← AiCallExecutor.call (provider 라우팅 포함)
 *   domain=openai_client   phase=ai_http_call    ← OpenAiClient.generate (순수 HTTP)
 * </pre>
 *
 * 예외 발생 시에도 finally 로 경과 시간을 기록하며,
 * 타임아웃·연결 오류는 {@code outcome=timeout|connection_error} 로 분류된다.
 */
@Aspect
@Component
public class AiTimingAspect {

    @Around("@annotation(aiTimed)")
    public Object measure(ProceedingJoinPoint pjp, AiTimed aiTimed) throws Throwable {
        long start = AiTimingLogger.start();
        String outcome = "success";
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            outcome = classifyOutcome(t);
            throw t;
        } finally {
            AiTimingLogger.end(aiTimed.domain(), aiTimed.phase(), start,
                    "outcome", outcome,
                    "class", pjp.getSignature().getDeclaringType().getSimpleName());
        }
    }

    private String classifyOutcome(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("timed out") || lower.contains("timeout")) {
                    return "timeout";
                }
                if (lower.contains("connection")) {
                    return "connection_error";
                }
            }
            cur = cur.getCause();
        }
        return "error";
    }
}
