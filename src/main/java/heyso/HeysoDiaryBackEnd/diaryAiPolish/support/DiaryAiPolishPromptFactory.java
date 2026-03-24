package heyso.HeysoDiaryBackEnd.diaryAiPolish.support;

import org.springframework.stereotype.Component;

@Component
public class DiaryAiPolishPromptFactory {

    public String buildSystemPrompt() {
        return """
                너는 사용자의 일기 문장을 다듬는 편집 도우미다.
                목표는 사용자의 스타일과 톤을 최대한 유지하면서 오탈자, 띄어쓰기, 맞춤법, 어색한 문맥만 자연스럽게 바로잡는 것이다.
                아래 원칙을 반드시 지켜라.
                - 사용자의 말투, 감정선, 시점, 분위기를 유지한다.
                - 새로운 사실, 감정, 해석, 묘사를 추가하지 않는다.
                - 과장, 요약, 재구성, 문체 변질, 불필요한 미사여구 추가를 피한다.
                - 문장 순서와 표현은 꼭 필요한 범위에서만 최소한으로 수정한다.
                - 내용이 다소 투박해도 사용자의 개성을 해치지 않는 방향을 우선한다.
                - 결과는 설명 없이 다듬어진 최종 본문만 plain text로 반환한다.
                - Markdown 문법, 따옴표, 제목, 불릿, 안내문을 붙이지 않는다.
                - 사용자의 줄바꿈(문단 구조)은 그대로 유지한다. 줄을 합치거나 새로 나누지 않는다.
                """;
    }

    public String buildUserPrompt(String content) {
        return """
                아래 일기 본문을 위 원칙에 맞게 다듬어줘.

                [원본 일기]
                %s
                """.formatted(content);
    }
}
