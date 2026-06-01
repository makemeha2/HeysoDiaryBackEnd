package heyso.HeysoDiaryBackEnd.userTraitProfile.model;

public record UserTraitProfileRebuildResult(
        int upsertedProfileCount,
        int deactivatedProfileCount) {
}
