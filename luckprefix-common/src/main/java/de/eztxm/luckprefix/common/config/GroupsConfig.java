package de.eztxm.luckprefix.common.config;

import de.eztxm.luckprefix.common.util.Encoder;

import java.nio.file.Path;

public final class GroupsConfig extends AbstractConfig {

    public GroupsConfig(Path filePath) {
        super(filePath);
    }

    @Override
    protected void defineDefaults() {
        addDefault("default.Prefix", "<gray>Player");
        addDefault("default.Suffix", "");
        addDefault("default.Tabformat", "<prefix> <dark_gray>- <gray><player>");
        addDefault("default.Chatformat", "<prefix> <dark_gray>- <gray><player> <dark_gray>» <gray><message>");
        addDefault("default.SortID", 999);
        addDefault("default.NameColor", "gray");

        saveDefaults();

        setComments("default", java.util.List.of("The name of the group"));
        setComments("default.Prefix", java.util.List.of(
                "Prefix/Suffix/Tabformat/Chatformat use Adventure MiniMessage,",
                "but legacy '&' color codes are also supported.",
                "https://docs.advntr.dev/minimessage/format.html"
        ));
        setComments("default.SortID", java.util.List.of(
                "Sort-ID defines tablist order (1..999). Lower is higher on the list."
        ));
        setComments("default.NameColor", java.util.List.of("Name color above the player"));

        saveComments();
    }

    public boolean hasGroup(String rawGroupName) {
        String sectionKey = Encoder.key(rawGroupName);
        return contains(sectionKey);
    }

    public String getPrefix(String rawGroupName) {
        return getString(Encoder.path(rawGroupName, "Prefix"), "");
    }

    public String getSuffix(String rawGroupName) {
        return getString(Encoder.path(rawGroupName, "Suffix"), "");
    }

    public String getTabFormat(String rawGroupName) {
        return getString(Encoder.path(rawGroupName, "Tabformat"),
                "<prefix> <dark_gray>- <gray><player>");
    }

    public String getChatFormat(String rawGroupName) {
        return getString(Encoder.path(rawGroupName, "Chatformat"),
                "<prefix> <dark_gray>- <gray><player> <dark_gray>» <gray><message>");
    }

    public int getSortId(String rawGroupName) {
        return getInt(Encoder.path(rawGroupName, "SortID"), 999);
    }

    public String getNameColor(String rawGroupName) {
        return getString(Encoder.path(rawGroupName, "NameColor"), "gray");
    }

    public void setPrefix(String rawGroupName, String value) {
        set(Encoder.path(rawGroupName, "Prefix"), value);
    }

    public void setSuffix(String rawGroupName, String value) {
        set(Encoder.path(rawGroupName, "Suffix"), value);
    }

    public void setTabFormat(String rawGroupName, String value) {
        set(Encoder.path(rawGroupName, "Tabformat"), value);
    }

    public void setChatFormat(String rawGroupName, String value) {
        set(Encoder.path(rawGroupName, "Chatformat"), value);
    }

    public void setSortId(String rawGroupName, int sortId) {
        set(Encoder.path(rawGroupName, "SortID"), sortId);
    }

    public void setNameColor(String rawGroupName, String colorName) {
        set(Encoder.path(rawGroupName, "NameColor"), colorName);
    }

    public void ensureGroupDefaults(String rawGroupName, boolean autoAdd, java.util.function.Consumer<String> warn) {
        String sectionKey = Encoder.key(rawGroupName);

        if (!contains(sectionKey)) {
            if (!autoAdd) {
                if (warn != null) warn.accept("Group '" + rawGroupName + "' not found — skipping.");
                return;
            }
            set(sectionKey, new java.util.LinkedHashMap<String, Object>());
            if (warn != null)
                warn.accept("groups.yml: group '" + rawGroupName + "' was missing — creating with defaults.");
        }
        setIfMissing(Encoder.path(rawGroupName, "Prefix"), "<gray>" + rawGroupName, warn, rawGroupName, "Prefix");
        setIfMissing(Encoder.path(rawGroupName, "Suffix"), "", warn, rawGroupName, "Suffix");
        setIfMissing(Encoder.path(rawGroupName, "Tabformat"), "<prefix> <dark_gray>- <gray><player>", warn, rawGroupName, "Tabformat");
        setIfMissing(Encoder.path(rawGroupName, "Chatformat"), "<prefix> <dark_gray>- <gray><player> <dark_gray>» <gray><message>", warn, rawGroupName, "Chatformat");
        if (!isSet(Encoder.path(rawGroupName, "SortID"))) {
            set(Encoder.path(rawGroupName, "SortID"), 999);
            if (warn != null) warn.accept("groups.yml: '" + rawGroupName + ".SortID' was missing — defaulting to 999.");
        }
        setIfMissing(Encoder.path(rawGroupName, "NameColor"), "gray", warn, rawGroupName, "NameColor");
    }

    private void setIfMissing(String dottedPath, Object defaultValue,
                              java.util.function.Consumer<String> warn,
                              String groupName, String keyName) {
        if (isSet(dottedPath)) return;
        set(dottedPath, defaultValue);
        if (warn != null)
            warn.accept("groups.yml: '" + groupName + "." + keyName + "' was missing — setting default: " + defaultValue);
    }
}