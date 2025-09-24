package de.eztxm.luckprefix.common.util;


public final class Encoder {
    private static final String COLON_ESC = "_";

    private Encoder() {
    }

    public static String key(String rawGroup) {
        return rawGroup.toLowerCase().replace(":", COLON_ESC);
    }

    public static String path(String rawGroup, String field) {
        return key(rawGroup.toLowerCase()) + "." + field;
    }
}
