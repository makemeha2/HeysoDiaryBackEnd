package heyso.HeysoDiaryBackEnd.monitoringMng.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public final class StackTraceUtils {

    private static final String APP_PREFIX = "heyso.";
    private static final int MAX_EXTERNAL_FRAMES = 5;
    private static final int MAX_LENGTH = 2000;
    private static final String TRUNCATED_SUFFIX = "... (truncated)";
    private static final String[] EXCLUDED_PREFIXES = {
            "java.base/",
            "jdk.internal.",
            "sun.reflect.",
            "org.springframework.",
            "org.apache.",
            "jakarta.servlet."
    };

    private StackTraceUtils() {
    }

    // Keep the throwable hierarchy readable while stripping low-signal framework frames.
    public static String summarize(String stackTrace) {
        if (StringUtils.isBlank(stackTrace)) {
            return "-";
        }

        String normalized = stackTrace.replace("\r\n", "\n").replace('\r', '\n');
        List<String> lines = splitLines(normalized);
        if (lines.isEmpty()) {
            return "-";
        }

        StringBuilder result = new StringBuilder();
        appendLine(result, lines.get(0));

        int index = 1;
        while (index < lines.size()) {
            String line = lines.get(index);
            if (isBlockHeader(line)) {
                index = appendBlock(result, lines, index);
                continue;
            }
            index = appendRootBlock(result, lines, index);
        }

        String summarized = result.toString().trim();
        if (summarized.isEmpty()) {
            return "-";
        }
        if (summarized.length() <= MAX_LENGTH) {
            return summarized;
        }
        return summarized.substring(0, MAX_LENGTH - TRUNCATED_SUFFIX.length()) + TRUNCATED_SUFFIX;
    }

    private static int appendRootBlock(StringBuilder result, List<String> lines, int start) {
        List<String> blockLines = new ArrayList<>();
        int index = start;
        while (index < lines.size() && !isBlockHeader(lines.get(index))) {
            blockLines.add(lines.get(index));
            index++;
        }

        appendFilteredFrames(result, blockLines);
        return index;
    }

    private static int appendBlock(StringBuilder result, List<String> lines, int start) {
        appendLine(result, lines.get(start));

        List<String> blockLines = new ArrayList<>();
        int index = start + 1;
        while (index < lines.size() && !isBlockHeader(lines.get(index))) {
            blockLines.add(lines.get(index));
            index++;
        }

        appendFilteredFrames(result, blockLines);
        return index;
    }

    private static void appendFilteredFrames(StringBuilder result, List<String> blockLines) {
        List<String> kept = new ArrayList<>();
        Deque<String> externalBuffer = new ArrayDeque<>();

        for (String line : blockLines) {
            if (isFrameLine(line)) {
                String frameClass = extractFrameClass(line);
                if (isAppFrame(frameClass)) {
                    // Preserve a small lead-in of non-excluded frames before the app frame.
                    while (!externalBuffer.isEmpty()) {
                        kept.add(externalBuffer.removeFirst());
                    }
                    kept.add(line);
                    continue;
                }

                if (!isExcludedFrame(frameClass)) {
                    if (externalBuffer.size() == MAX_EXTERNAL_FRAMES) {
                        externalBuffer.removeFirst();
                    }
                    externalBuffer.addLast(line);
                }
                continue;
            }

            if (isCommonFramesOmitted(line)) {
                kept.add(line);
            }
        }

        // If no app frame exists in this block, fall back to the latest useful external frames.
        if (kept.stream().noneMatch(StackTraceUtils::isFrameLine)) {
            while (!externalBuffer.isEmpty()) {
                kept.add(externalBuffer.removeFirst());
            }
        }

        for (String line : kept) {
            appendLine(result, line);
        }
    }

    private static boolean isBlockHeader(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("Caused by:") || trimmed.startsWith("Suppressed:");
    }

    private static boolean isFrameLine(String line) {
        return line.trim().startsWith("at ");
    }

    private static boolean isCommonFramesOmitted(String line) {
        return line.trim().startsWith("...");
    }

    private static boolean isAppFrame(String frameClass) {
        return frameClass.startsWith(APP_PREFIX);
    }

    private static boolean isExcludedFrame(String frameClass) {
        if (frameClass.isEmpty()) {
            return false;
        }

        if (frameClass.startsWith("java.base/")) {
            return true;
        }

        String normalized = frameClass.startsWith("java.base/")
                ? frameClass.substring("java.base/".length())
                : frameClass;

        for (String excludedPrefix : EXCLUDED_PREFIXES) {
            if (frameClass.startsWith(excludedPrefix) || normalized.startsWith(excludedPrefix)) {
                return true;
            }
        }
        return false;
    }

    private static String extractFrameClass(String line) {
        String trimmed = line.trim();
        if (!trimmed.startsWith("at ")) {
            return "";
        }

        int methodStart = 3;
        int parenIndex = trimmed.indexOf('(');
        String invocation = parenIndex >= 0 ? trimmed.substring(methodStart, parenIndex) : trimmed.substring(methodStart);
        int methodIndex = invocation.lastIndexOf('.');
        return methodIndex > 0 ? invocation.substring(0, methodIndex) : invocation;
    }

    private static List<String> splitLines(String value) {
        List<String> lines = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '\n') {
                lines.add(value.substring(start, i));
                start = i + 1;
            }
        }
        if (start <= value.length()) {
            lines.add(value.substring(start));
        }
        return lines;
    }

    private static void appendLine(StringBuilder builder, String line) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(line);
    }
}
