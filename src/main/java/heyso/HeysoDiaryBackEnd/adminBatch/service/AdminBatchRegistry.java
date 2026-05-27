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
                        runners.get("diaryAnalysisBatchRunner")));
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
