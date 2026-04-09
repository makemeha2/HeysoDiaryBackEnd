package heyso.HeysoDiaryBackEnd.ai.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiPromptTemplateMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.mapper.AiPromptTemplateRelMapper;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptTemplate;
import heyso.HeysoDiaryBackEnd.aiTemplate.model.AiPromptTemplateRel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiTemplateRenderer {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    private static final String INCLUDE_PREFIX = "> ";

    private final AiPromptTemplateMapper aiPromptTemplateMapper;
    private final AiPromptTemplateRelMapper aiPromptTemplateRelMapper;

    public String render(Long templateId, Map<String, String> variables) {
        AiPromptTemplate template = aiPromptTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + templateId);
        }

        String assembled = assembleContent(template);
        return substituteVariables(assembled, variables);
    }

    private String assembleContent(AiPromptTemplate template) {
        if (!"ROOT".equals(template.getTemplateType())) {
            return template.getContent();
        }

        List<AiPromptTemplateRel> allRels = aiPromptTemplateRelMapper.selectByParentId(template.getTemplateId());
        List<AiPromptTemplateRel> activeRels = allRels.stream()
                .filter(r -> r.getIsActive() != null && r.getIsActive() == 1)
                .toList();

        List<String> prependParts = new ArrayList<>();
        List<String> appendParts = new ArrayList<>();

        for (AiPromptTemplateRel rel : activeRels) {
            AiPromptTemplate child = aiPromptTemplateMapper.selectById(rel.getChildTemplateId());
            if (child == null) {
                continue;
            }
            String childContent = child.getContent() != null ? child.getContent() : "";
            if ("PREPEND".equals(rel.getMergeType())) {
                prependParts.add(childContent);
            } else if ("APPEND".equals(rel.getMergeType())) {
                appendParts.add(childContent);
            }
        }

        // PREPEND: sort_seq DESC order (higher seq first, as they were added last and go before root)
        // The rels are already sorted by sort_seq ASC from the mapper, so we reverse for PREPEND
        List<String> prependSorted = new ArrayList<>(prependParts);
        java.util.Collections.reverse(prependSorted);

        StringBuilder sb = new StringBuilder();
        for (String part : prependSorted) {
            if (!part.isBlank()) {
                sb.append(part).append("\n");
            }
        }
        sb.append(template.getContent() != null ? template.getContent() : "");
        for (String part : appendParts) {
            if (!part.isBlank()) {
                sb.append("\n").append(part);
            }
        }

        return sb.toString();
    }

    private String substituteVariables(String content, Map<String, String> variables) {
        if (content == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);

        while (matcher.find()) {
            String key = matcher.group(1).trim();

            if (key.startsWith(INCLUDE_PREFIX)) {
                String templateKey = key.substring(INCLUDE_PREFIX.length()).trim();
                String replacement = resolveTemplateInclude(templateKey, variables);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } else {
                String value = (variables != null) ? variables.getOrDefault(key, "") : "";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(value != null ? value : ""));
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private String resolveTemplateInclude(String templateKey, Map<String, String> variables) {
        AiPromptTemplate included = aiPromptTemplateMapper.selectByKey(templateKey);
        if (included == null || included.getContent() == null) {
            return "";
        }
        return substituteVariables(included.getContent(), variables);
    }
}
