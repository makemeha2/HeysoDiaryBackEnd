# agent.md — HeysoDiaryBackEnd AI Chat 개발 가이드 (Lombok + MapStruct 패턴)
본 문서는 `HeysoDiaryBackEnd` 프로젝트의 `aichat` 모듈을 개발할 때, DTO/Model/Service/Mapper 코딩 스타일을 일관되게 유지하기 위한 가이드다.
---
## 1. 목표

- Service 레이어는 **비즈니스 흐름만** 남기고, 반복/보일러플레이트 코드는 최소화한다.
- DTO ↔ Model 변환은 **MapStruct**로 통일한다.
- Model(=MyBatis 매핑 모델) 객체 생성은 **Lombok Builder**로 통일한다.


## 2. Lombok 사용 규칙 (Model / DTO)

### 2.1 Model(MyBatis 모델) 규칙
- MyBatis/프레임워크 호환을 위해 아래를 기본으로 사용한다:

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage { ... }

필수
@NoArgsConstructor : MyBatis가 기본 생성자로 객체를 만들 수 있어야 함
@Getter/@Setter : MyBatis 결과 매핑 및 서비스에서 편의
@Builder : 서비스 레이어에서 setter 덩어리를 없애기 위함
@AllArgsConstructor : Builder 내부 사용

선택
@Builder.Default : 기본값이 항상 고정일 때만 사용 (예: contentFormat 기본이 항상 "markdown" 인 경우)

2.2 DTO 규칙
DTO는 목적에 따라 아래 중 하나를 사용한다.

Request DTO
검증이 필요하면 jakarta.validation으로 선언형 검증을 넣는다.

Lombok은 @Getter/@Setter 또는 @Getter + 생성자 패턴을 사용한다.

Response DTO
immutable을 선호한다면:
@Getter + @Builder 또는 record 사용
단, 기존 패턴과 충돌이 없게 유지한다.

4. MapStruct 사용 규칙 (DTO ↔ Model 변환)
4.1 기본 원칙
Service 레이어에서 .stream().map(Dto::from) 패턴을 없애고,
DTO 변환은 MapStruct Mapper를 통해 수행한다.

Mapper는 Spring Bean으로 등록한다:
@Mapper(componentModel = "spring")

누락 필드를 컴파일 타임에 잡기 위해 아래 정책을 권장한다:
unmappedTargetPolicy = ReportingPolicy.ERROR

3.2 Mapper 분리 원칙
AiChatDtoMapper : Model → Response DTO 변환 전용(추천)
(선택) AiChatCommandMapper : Request DTO → Model 변환 전용

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AiChatDtoMapper {
    ChatMessageResponse toMessageResponse(ChatMessage message);
    List<ChatMessageResponse> toMessageResponses(List<ChatMessage> messages);

    ChatConversationListItem toConversationListItem(ChatConversation conversation);
    List<ChatConversationListItem> toConversationListItems(List<ChatConversation> conversations);

    ChatSummaryResponse toSummaryResponse(ChatConversationSummary summary);
}

3.4 서비스에서 사용 예시
List<ChatMessageResponse> dtos = dtoMapper.toMessageResponses(messages);
return ChatMessageListResponse.of(dtos);