package heyso.HeysoDiaryBackEnd.userMemorySnapshot.model;

public record UserMemorySnapshotParsedResponse(
        String summaryText,
        String recurringThemesJson,
        String importantPeopleJson,
        String stressFactorsJson,
        String recoveryFactorsJson,
        String traitSummaryJson) {
}
