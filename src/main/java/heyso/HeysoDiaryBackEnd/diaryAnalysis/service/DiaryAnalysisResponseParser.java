package heyso.HeysoDiaryBackEnd.diaryAnalysis.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisAiResponse;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisCandidate;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisErrorCode;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryAnalysisResult;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryEvent;
import heyso.HeysoDiaryBackEnd.diaryAnalysis.model.DiaryTraitEvidence;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiaryAnalysisResponseParser {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal MINUS_ONE = BigDecimal.ONE.negate();

    private final ObjectMapper objectMapper;

    public DiaryAnalysisResult parse(String content, DiaryAnalysisCandidate candidate,
            Set<String> eventTypes, Set<String> emotions, Set<String> traitKeys) {
        String json = stripCodeFence(content);
        DiaryAnalysisAiResponse response = readResponse(json);
        validate(response, eventTypes, emotions, traitKeys);

        List<DiaryEvent> events = toEvents(response.getEvents(), candidate);
        List<DiaryTraitEvidence> evidence = toTraitEvidence(response.getTraitEvidence(), candidate);
        return new DiaryAnalysisResult(response.getSummary().trim(), events, evidence, compactJson(json));
    }

    private DiaryAnalysisAiResponse readResponse(String json) {
        try {
            return objectMapper.readValue(json, DiaryAnalysisAiResponse.class);
        } catch (Exception e) {
            throw new DiaryAnalysisException(DiaryAnalysisErrorCode.JSON_PARSE_FAILED,
                    "AI diary analysis response is not valid JSON", e);
        }
    }

    private void validate(DiaryAnalysisAiResponse response, Set<String> eventTypes,
            Set<String> emotions, Set<String> traitKeys) {
        if (response == null || StringUtils.isBlank(response.getSummary())) {
            throw invalid("summary is required");
        }
        for (DiaryAnalysisAiResponse.EventItem event : safeList(response.getEvents())) {
            if (event == null) {
                throw invalid("event item is null");
            }
            if (!eventTypes.contains(StringUtils.defaultString(event.getEventType()))) {
                throw invalid("unknown event_type: " + event.getEventType());
            }
            if (StringUtils.isNotBlank(event.getEmotion()) && !emotions.contains(event.getEmotion())) {
                throw invalid("unknown emotion: " + event.getEmotion());
            }
            requireRange(event.getEmotionIntensity(), ZERO, ONE, "emotion_intensity");
            requireRange(event.getConfidence(), ZERO, ONE, "event confidence");
        }
        for (DiaryAnalysisAiResponse.TraitEvidenceItem evidence : safeList(response.getTraitEvidence())) {
            if (evidence == null) {
                throw invalid("trait evidence item is null");
            }
            if (!traitKeys.contains(StringUtils.defaultString(evidence.getTraitKey()))) {
                throw invalid("unknown trait_key: " + evidence.getTraitKey());
            }
            requireRange(evidence.getSignalScore(), MINUS_ONE, ONE, "signal_score");
            requireRange(evidence.getConfidence(), ZERO, ONE, "trait confidence");
        }
    }

    private List<DiaryEvent> toEvents(List<DiaryAnalysisAiResponse.EventItem> items,
            DiaryAnalysisCandidate candidate) {
        List<DiaryEvent> events = new ArrayList<>();
        for (DiaryAnalysisAiResponse.EventItem item : safeList(items)) {
            DiaryEvent event = new DiaryEvent();
            event.setDiaryId(candidate.getDiaryId());
            event.setUserId(candidate.getUserId());
            event.setEventType(item.getEventType());
            event.setEventTitle(blankToNull(item.getEventTitle()));
            event.setEventSummary(blankToNull(item.getEventSummary()));
            event.setEmotion(blankToNull(item.getEmotion()));
            event.setEmotionIntensity(item.getEmotionIntensity());
            event.setPeopleJson(toJson(item.getPeople()));
            event.setPlacesJson(toJson(item.getPlaces()));
            event.setActivitiesJson(toJson(item.getActivities()));
            event.setRelationshipJson(toJson(item.getRelationship()));
            event.setCausalityJson(toJson(item.getCausality()));
            event.setPatternCandidateJson(toJson(item.getPatternCandidate()));
            event.setConfidence(defaultDecimal(item.getConfidence()));
            event.setEvidenceText(blankToNull(item.getEvidenceText()));
            events.add(event);
        }
        return events;
    }

    private List<DiaryTraitEvidence> toTraitEvidence(List<DiaryAnalysisAiResponse.TraitEvidenceItem> items,
            DiaryAnalysisCandidate candidate) {
        List<DiaryTraitEvidence> evidenceList = new ArrayList<>();
        for (DiaryAnalysisAiResponse.TraitEvidenceItem item : safeList(items)) {
            DiaryTraitEvidence evidence = new DiaryTraitEvidence();
            evidence.setDiaryId(candidate.getDiaryId());
            evidence.setUserId(candidate.getUserId());
            evidence.setTraitKey(item.getTraitKey());
            evidence.setSignalScore(defaultDecimal(item.getSignalScore()));
            evidence.setConfidence(defaultDecimal(item.getConfidence()));
            evidence.setEvidenceText(blankToNull(item.getEvidenceText()));
            evidence.setReasonJson(toJson(item.getReason()));
            evidenceList.add(evidence);
        }
        return evidenceList;
    }

    private String stripCodeFence(String content) {
        String trimmed = StringUtils.defaultString(content).trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstLineEnd = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
            return trimmed.substring(firstLineEnd + 1, lastFence).trim();
        }
        return trimmed;
    }

    private String compactJson(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return json;
        }
    }

    private String toJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw invalid("invalid nested JSON");
        }
    }

    private void requireRange(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName) {
        if (value == null) {
            return;
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw invalid(fieldName + " is out of range");
        }
    }

    private DiaryAnalysisException invalid(String message) {
        return new DiaryAnalysisException(DiaryAnalysisErrorCode.SCHEMA_INVALID, message);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private String blankToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private <T> List<T> safeList(List<T> items) {
        return items == null ? Collections.emptyList() : items;
    }
}
