package heyso.HeysoDiaryBackEnd.utils;

import java.text.Normalizer;

public class TextSnippetUtil {
    private TextSnippetUtil() {
    }

    /**
     * 텍스트를 AI 프롬프트에 넣기 좋게 정규화 + 길이 제한
     * - null -> ""
     * - 유니코드 정규화(NFKC)로 특수문자/호환문자 처리 안정화
     * - \r\n, \r -> \n 통일
     * - 탭/여러 공백 정리
     * - 앞뒤 공백 제거
     * - maxChars 초과 시 잘라내고 "…" 붙임(최소 1자라도 남기려고 안전 처리)
     */
    public static String normalizeAndLimit(String content, int maxChars) {
        if (content == null)
            return "";

        // 1) 유니코드 정규화 (선택이지만, 텍스트가 다양할 때 안정적)
        String s = Normalizer.normalize(content, Normalizer.Form.NFKC);

        // 2) 개행 통일
        s = s.replace("\r\n", "\n").replace("\r", "\n");

        // 3) 탭 제거(공백으로)
        s = s.replace("\t", " ");

        // 4) 여러 공백 정리 (개행은 유지)
        // - " " -> " "
        // - 줄마다 앞뒤 공백은 trim에서 처리
        s = s.replaceAll("[ ]{2,}", " ");

        // 5) 앞뒤 공백 제거
        s = s.trim();

        // 6) 길이 제한
        if (maxChars <= 0)
            return "";
        if (s.length() <= maxChars)
            return s;

        // maxChars가 너무 작아도 "…"를 붙일 수 있도록 안전 처리
        if (maxChars == 1)
            return "…";
        return s.substring(0, maxChars - 1) + "…";
    }
}
