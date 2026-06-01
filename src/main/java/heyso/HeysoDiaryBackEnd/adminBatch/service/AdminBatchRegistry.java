package heyso.HeysoDiaryBackEnd.adminBatch.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchDefinition;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchKeys;
import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunner;

@Component
public class AdminBatchRegistry {
    private final List<AdminBatchDefinition> definitions;
    private final Map<String, AdminBatchDefinition> definitionMap;

    public AdminBatchRegistry(
            @Value("${app.diary.summary.scheduler.cron:0 0 3 * * *}") String diarySummaryCron,
            @Value("${app.diary.analysis.scheduler.cron:0 */10 * * * *}") String diaryAnalysisCron,
            @Value("${app.user-trait-profile.scheduler.cron:0 0 4 * * *}") String userTraitProfileCron,
            Map<String, AdminBatchRunner> runners) {
        this.definitions = List.of(
                new AdminBatchDefinition(
                        AdminBatchKeys.DIARY_SUMMARY_REBUILD,
                        "일기 집계 및 캐시 재생성",
                        "dirty 상태의 사용자 일기 집계 캐시를 재집계합니다.",
                        diarySummaryCron,
                        "Asia/Seoul",
                        runners.get("diarySummaryBatchRunner")),
                new AdminBatchDefinition(
                        AdminBatchKeys.DIARY_ANALYSIS,
                        "일기 장기 메모리 분석",
                        "마지막 수정 후 1시간 지난 dirty 일기를 AI로 구조화 분석합니다.",
                        diaryAnalysisCron,
                        "Asia/Seoul",
                        runners.get("diaryAnalysisBatchRunner")),
                new AdminBatchDefinition(
                        AdminBatchKeys.USER_TRAIT_PROFILE,
                        "사용자 trait profile 집계",
                        "active trait evidence를 사용자 단위 장기 profile로 집계합니다.",
                        userTraitProfileCron,
                        "Asia/Seoul",
                        runners.get("userTraitProfileBatchRunner")),
                new AdminBatchDefinition(
                        AdminBatchKeys.USER_MEMORY_SNAPSHOT,
                        "사용자 장기 메모리 snapshot 생성",
                        "active event와 trait profile을 AI로 요약해 최신 memory snapshot을 생성합니다.",
                        "profile batch 직후 실행",
                        "Asia/Seoul",
                        runners.get("userMemorySnapshotBatchRunner")));
        this.definitionMap = definitions.stream()
                .collect(Collectors.toUnmodifiableMap(AdminBatchDefinition::batchKey, Function.identity()));
    }

    public List<AdminBatchDefinition> getDefinitions() {
        return definitions;
    }

    public AdminBatchDefinition getDefinition(String batchKey) {
        return definitionMap.get(batchKey);
    }
}
