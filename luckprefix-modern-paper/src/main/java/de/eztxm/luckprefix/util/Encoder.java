package de.eztxm.luckprefix.util;

import org.jetbrains.annotations.NotNull;

public class Encoder {
    private static final String COLON_ESC = "__colon__";

    private Encoder() {}

    @NotNull
    public static String key(@NotNull String rawGroup) {
        return rawGroup.replace(":", COLON_ESC);
    }

    @NotNull
    public static String path(@NotNull String rawGroup, @NotNull String field) {
        return key(rawGroup) + "." + field;
    }
}
