package de.eztxm.luckprefix.common.util;

import java.util.regex.Pattern;

public final class Redactor {
    private static final Pattern SECRET_LINE =
            Pattern.compile("(?i)(password|pass|token|secret|apikey|api_key)\\s*[:=]\\s*.*");

    private Redactor() {}

    public static String redactLine(String line) {
        if (line == null) return "";
        if (SECRET_LINE.matcher(line).find()) {
            int idx = line.indexOf(':');
            if (idx < 0) idx = line.indexOf('=');
            if (idx > 0) return line.substring(0, idx + 1) + " ******";
            return "******";
        }
        return line;
    }

    public static String redactBlock(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (String line : text.split("\\r?\\n", -1)) {
            sb.append(redactLine(line)).append('\n');
        }
        return sb.toString();
    }
}
