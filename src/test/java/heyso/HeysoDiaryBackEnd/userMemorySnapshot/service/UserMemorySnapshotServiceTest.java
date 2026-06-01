package heyso.HeysoDiaryBackEnd.userMemorySnapshot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import heyso.HeysoDiaryBackEnd.adminBatch.support.AdminBatchRunResult;
import heyso.HeysoDiaryBackEnd.ai.client.AiProvider;
import heyso.HeysoDiaryBackEnd.ai.support.AiModelResolver;
import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptBinding;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.mapper.UserMemorySnapshotMapper;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshot;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotEventSource;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotParsedResponse;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotProfileSource;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.model.UserMemorySnapshotRebuildResult;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.support.UserMemorySnapshotAiClient;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.support.UserMemorySnapshotAiClient.ResolvedSnapshotPrompt;
import heyso.HeysoDiaryBackEnd.userMemorySnapshot.support.UserMemorySnapshotResponseParser;

@ExtendWith(MockitoExtension.class)
class UserMemorySnapshotServiceTest {
    @Mock
    private UserMemorySnapshotMapper userMemorySnapshotMapper;

    @Mock
    private UserMemorySnapshotAiClient aiClient;

    @Mock
    private UserMemorySnapshotResponseParser responseParser;

    @Mock
    private UserMemorySnapshotPersistenceService persistenceService;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private UserMemorySnapshotService service;

    @BeforeEach
    void setUp() {
        service = new UserMemorySnapshotService(
                userMemorySnapshotMapper,
                aiClient,
                responseParser,
                persistenceService,
                objectMapper);
    }

    @Test
    @DisplayName("변경 사용자가 없으면 persistence 호출 없이 종료한다")
    void rebuildChangedUserSnapshots_returnsEmptyResultWhenNoChangedUsers() {
        when(userMemorySnapshotMapper.selectChangedUserIds()).thenReturn(List.of());

        AdminBatchRunResult result = service.rebuildChangedUserSnapshots();

        assertThat(result.successCount()).isZero();
        assertThat(result.failureCount()).isZero();
        assertThat(result.message()).contains("생성 대상 사용자가 없습니다");
        verifyNoInteractions(persistenceService);
    }

    @Test
    @DisplayName("source가 모두 비어 있으면 AI 호출 없이 skip한다")
    void rebuildUserSnapshot_skipsWhenSourceIsEmpty() {
        LocalDate from = LocalDate.of(2026, 3, 4);
        LocalDate to = LocalDate.of(2026, 6, 1);
        when(userMemorySnapshotMapper.selectActiveEventSources(10L, from, to)).thenReturn(List.of());
        when(userMemorySnapshotMapper.selectActiveProfileSources(10L)).thenReturn(List.of());

        UserMemorySnapshotRebuildResult result = service.rebuildUserSnapshot(10L, from, to);

        assertThat(result.createdSnapshotCount()).isZero();
        assertThat(result.skippedCount()).isEqualTo(1);
        verifyNoInteractions(aiClient, responseParser, persistenceService);
    }

    @Test
    @DisplayName("AI 성공 결과를 active snapshot 저장 모델로 넘긴다")
    void rebuildUserSnapshot_persistsParsedSnapshot() {
        LocalDate from = LocalDate.of(2026, 3, 4);
        LocalDate to = LocalDate.of(2026, 6, 1);
        UserMemorySnapshotEventSource event = new UserMemorySnapshotEventSource();
        event.setEventId(100L);
        event.setDiaryDate(LocalDate.of(2026, 5, 30));
        event.setEventSummary("친구와 대화했다.");
        event.setUpdatedAt(LocalDateTime.of(2026, 5, 30, 10, 0));
        UserMemorySnapshotProfileSource profile = new UserMemorySnapshotProfileSource();
        profile.setProfileId(200L);
        profile.setTraitKey("SELF_REFLECTION");
        profile.setUpdatedAt(LocalDateTime.of(2026, 5, 31, 10, 0));

        when(userMemorySnapshotMapper.selectActiveEventSources(10L, from, to)).thenReturn(List.of(event));
        when(userMemorySnapshotMapper.selectActiveProfileSources(10L)).thenReturn(List.of(profile));
        ResolvedSnapshotPrompt prompt = prompt();
        when(aiClient.resolve(any())).thenReturn(prompt);
        when(aiClient.execute(prompt)).thenReturn("{json}");
        when(responseParser.parse("{json}")).thenReturn(new UserMemorySnapshotParsedResponse(
                "요약",
                "[]",
                "[]",
                "[]",
                "[]",
                "[]"));

        UserMemorySnapshotRebuildResult result = service.rebuildUserSnapshot(10L, from, to);

        assertThat(result.createdSnapshotCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isZero();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(aiClient).resolve(variablesCaptor.capture());
        assertThat(variablesCaptor.getValue()).containsKeys(
                "source_from_date", "source_to_date", "events_json", "trait_profiles_json");

        ArgumentCaptor<UserMemorySnapshot> snapshotCaptor = ArgumentCaptor.forClass(UserMemorySnapshot.class);
        verify(persistenceService).replaceActiveSnapshot(snapshotCaptor.capture());
        assertThat(snapshotCaptor.getValue().getSummaryText()).isEqualTo("요약");
        assertThat(snapshotCaptor.getValue().getSourceFromDate()).isEqualTo(from);
        assertThat(snapshotCaptor.getValue().getSourceToDate()).isEqualTo(to);
        assertThat(snapshotCaptor.getValue().getSourceJson()).contains("source_hash", "event_count", "profile_count");
    }

    @Test
    @DisplayName("사용자 단위 실패가 발생해도 다음 사용자를 계속 처리한다")
    void rebuildChangedUserSnapshots_continuesAfterUserFailure() {
        when(userMemorySnapshotMapper.selectChangedUserIds()).thenReturn(List.of(10L, 20L));
        when(userMemorySnapshotMapper.selectActiveEventSources(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new IllegalStateException("boom"));
        when(userMemorySnapshotMapper.selectActiveEventSources(eq(20L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(userMemorySnapshotMapper.selectActiveProfileSources(20L)).thenReturn(List.of(new UserMemorySnapshotProfileSource()));
        ResolvedSnapshotPrompt prompt = prompt();
        when(aiClient.resolve(any())).thenReturn(prompt);
        when(aiClient.execute(prompt)).thenReturn("{json}");
        when(responseParser.parse("{json}")).thenReturn(new UserMemorySnapshotParsedResponse(
                "요약", "[]", "[]", "[]", "[]", "[]"));

        AdminBatchRunResult result = service.rebuildChangedUserSnapshots();

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(1);
        assertThat(result.message()).contains("처리 대상 2명", "성공 1명", "실패 1명", "생성 snapshot 1건");
    }

    private ResolvedSnapshotPrompt prompt() {
        AiPromptBinding binding = new AiPromptBinding();
        binding.setBindingId(1L);
        binding.setSystemTemplateId(2L);
        binding.setUserTemplateId(3L);
        binding.setRuntimeProfileId(4L);
        AiRuntimeProfile profile = new AiRuntimeProfile();
        profile.setRuntimeProfileId(4L);
        return new ResolvedSnapshotPrompt(
                new AiPromptResolver.BindingResolution(binding, profile, "system", "user"),
                new AiModelResolver.ResolvedModel("gpt-4o-mini", AiProvider.OPENAI));
    }
}
