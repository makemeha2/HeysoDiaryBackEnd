package heyso.HeysoDiaryBackEnd.diaryAiPolish.type;

public enum DiaryAiPolishMode {
    STRICT("POLISH"),
    RELAXED("POLISH_RELAXED");

    private final String featureKey;

    DiaryAiPolishMode(String featureKey) {
        this.featureKey = featureKey;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public static DiaryAiPolishMode defaultMode() {
        return STRICT;
    }
}
