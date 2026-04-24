package heyso.HeysoDiaryBackEnd.diary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import heyso.HeysoDiaryBackEnd.ai.client.AiMessage;
import heyso.HeysoDiaryBackEnd.ai.client.AiProvider;
import heyso.HeysoDiaryBackEnd.ai.client.AiRequest;
import heyso.HeysoDiaryBackEnd.ai.client.AiResponse;
import heyso.HeysoDiaryBackEnd.ai.support.AiCallExecutor;
import heyso.HeysoDiaryBackEnd.ai.support.AiModelResolver;
import heyso.HeysoDiaryBackEnd.ai.support.AiPromptResolver;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptBinding;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiRuntimeProfile;
import heyso.HeysoDiaryBackEnd.diary.dto.DiaryNudgeResponse;
import heyso.HeysoDiaryBackEnd.diary.mapper.DiaryMapper;
import heyso.HeysoDiaryBackEnd.diary.model.Diary;

@ExtendWith(MockitoExtension.class)
class DiaryNudgeServiceTest {

        @Mock
        private DiaryMapper diaryMapper;

        @Mock
        private AiCallExecutor aiCallExecutor;

        @Mock
        private AiPromptResolver aiPromptResolver;

        @Mock
        private AiModelResolver aiModelResolver;

        @InjectMocks
        private DiaryNudgeService diaryNudgeService;

        @Test
        @DisplayName("DIARY_NUDGE binding 템플릿과 해석된 모델로 nudge AI 요청을 생성한다")
        void createTodayNudgeAsync_usesTemplateBindingAndResolvedModel() {
                Diary diary = new Diary();
                diary.setDiaryDate(LocalDate.of(2026, 4, 23));
                diary.setTitle("산책");
                diary.setContentMd("  오늘은 공원을 걸었다.  ");

                AiRuntimeProfile profile = AiRuntimeProfile.builder()
                                .temperature(new BigDecimal("0.7"))
                                .topP(new BigDecimal("0.9"))
                                .maxTokens(0)
                                .build();
                AiPromptResolver.BindingResolution resolution = new AiPromptResolver.BindingResolution(
                                AiPromptBinding.builder().build(),
                                profile,
                                "system prompt",
                                "user prompt");

                when(diaryMapper.selectLatestDiaryBeforeDate(eq(1L), any(LocalDate.class))).thenReturn(diary);
                when(aiPromptResolver.resolve(eq("DIARY_NUDGE"), eq("NUDGE"), any())).thenReturn(resolution);
                when(aiModelResolver.resolve(profile))
                                .thenReturn(new AiModelResolver.ResolvedModel("gpt-4o-mini", AiProvider.OPENAI));
                when(aiCallExecutor.call(any())).thenReturn(new AiResponse("  오늘은 어떤 장면이 마음에 남았나요?  ",
                                AiProvider.OPENAI, "gpt-4o-mini", "req-1", 1, 2, 3));

                DiaryNudgeResponse response = diaryNudgeService.createTodayNudgeAsync(1L).join();

                assertThat(response.getMessageText()).isEqualTo("오늘은 어떤 장면이 마음에 남았나요?");

                @SuppressWarnings("unchecked")
                ArgumentCaptor<Map<String, String>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
                verify(aiPromptResolver).resolve(eq("DIARY_NUDGE"), eq("NUDGE"), variablesCaptor.capture());
                assertThat(variablesCaptor.getValue())
                                .containsEntry("diary_date", "2026-04-23")
                                .containsEntry("title", "산책")
                                .containsEntry("content_snippet", "오늘은 공원을 걸었다.");

                ArgumentCaptor<AiRequest> requestCaptor = ArgumentCaptor.forClass(AiRequest.class);
                verify(aiCallExecutor).call(requestCaptor.capture());
                AiRequest request = requestCaptor.getValue();
                assertThat(request.provider()).isEqualTo(AiProvider.OPENAI);
                assertThat(request.model()).isEqualTo("gpt-4o-mini");
                assertThat(request.temperature()).isEqualTo(0.7);
                assertThat(request.topP()).isEqualTo(0.9);
                assertThat(request.maxTokens()).isNull();
                assertThat(request.messages())
                                .extracting(AiMessage::role, AiMessage::content)
                                .containsExactly(
                                                org.assertj.core.groups.Tuple.tuple("system", "system prompt"),
                                                org.assertj.core.groups.Tuple.tuple("user", "user prompt"));
        }

        @Test
        @DisplayName("resolver 또는 AI 호출 실패 시 기존처럼 빈 메시지를 반환한다")
        void createTodayNudgeAsync_returnsBlankMessage_whenResolverFails() {
                Diary diary = new Diary();
                diary.setDiaryDate(LocalDate.of(2026, 4, 23));
                diary.setTitle("산책");
                diary.setContentMd("내용");

                when(diaryMapper.selectLatestDiaryBeforeDate(eq(1L), any(LocalDate.class))).thenReturn(diary);
                when(aiPromptResolver.resolve(eq("DIARY_NUDGE"), eq("NUDGE"), any()))
                                .thenThrow(new IllegalStateException("binding not found"));

                DiaryNudgeResponse response = diaryNudgeService.createTodayNudgeAsync(1L).join();

                assertThat(response.getMessageText()).isEmpty();
        }
}
