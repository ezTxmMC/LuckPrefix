package de.eztxm.luckprefix.common.config;

import de.eztxm.luckprefix.common.logging.DebugLog;
import de.eztxm.luckprefix.common.util.Encoder;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public final class GroupsConfig extends AbstractConfig {

    public GroupsConfig(Path filePath, DebugLog debugLog) {
        super(filePath, debugLog);
        getDebugLog().info("GroupsConfig: constructed for " + filePath);
    }

    @Override
    protected void defineDefaults() {
        getDebugLog().info("GroupsConfig.defineDefaults: applying defaults");

        addDefault("default.Prefix", "<gray>Player");
        addDefault("default.Suffix", "");
        addDefault("default.Tabformat", "<prefix> <dark_gray>- <gray><player>");
        addDefault("default.Chatformat", "<prefix> <dark_gray>- <gray><player> <dark_gray>» <gray><message>");
        addDefault("default.SortID", 999);
        addDefault("default.NameColor", "gray");

        getDebugLog().debug("GroupsConfig.defineDefaults: defaults saved to disk");

        setComments("default", List.of("The name of the group"));
        setComments("default.Prefix", List.of(
                "Prefix/Suffix/Tabformat/Chatformat use Adventure MiniMessage,",
                "but legacy '&' color codes are also supported.",
                "https://docs.advntr.dev/minimessage/format.html"
        ));
        setComments("default.SortID", List.of(
                "Sort-ID defines tablist order (1..999). Lower is higher on the list."
        ));
        setComments("default.NameColor", List.of("Name color above the player"));

        getDebugLog().debug("GroupsConfig.defineDefaults: comments saved");
    }

    public boolean hasGroup(String rawGroupName) {
        String sectionKey = Encoder.key(rawGroupName);
        boolean present = contains(sectionKey);
        getDebugLog().debug("GroupsConfig.hasGroup: raw='" + rawGroupName + "' key='" + sectionKey + "' present=" + present);
        return present;
    }

    public String getPrefix(String rawGroupName) {
        String path = Encoder.path(rawGroupName, "Prefix");
        String value = getString(path, "");
        if (value.isEmpty()) getDebugLog().debug("GroupsConfig.getPrefix: missing → '' @ " + path);
        return value;
    }

    public String getSuffix(String rawGroupName) {
        String path = Encoder.path(rawGroupName, "Suffix");
        String value = getString(path, "");
        if (value.isEmpty()) getDebugLog().debug("GroupsConfig.getSuffix: missing → '' @ " + path);
        return value;
    }

    public String getTabFormat(String rawGroupName) {
        String path = Encoder.path(rawGroupName, "Tabformat");
        String def = "<prefix> <dark_gray>- <gray><player>";
        String value = getString(path, def);
        if (def.equals(value)) getDebugLog().debug("GroupsConfig.getTabFormat: missing → default @ " + path);
        return value;
    }

    public String getChatFormat(String rawGroupName) {
        String path = Encoder.path(rawGroupName, "Chatformat");
        String def = "<prefix> <dark_gray>- <gray><player> <dark_gray>» <gray><message>";
        String value = getString(path, def);
        if (def.equals(value)) getDebugLog().debug("GroupsConfig.getChatFormat: missing → default @ " + path);
        return value;
    }

    public int getSortId(String rawGroupName) {
        String path = Encoder.path(rawGroupName, "SortID");
        int value = getInt(path, 999);
        if (value == 999) getDebugLog().debug("GroupsConfig.getSortId: missing → 999 @ " + path);
        return value;
    }

    public String getNameColor(String rawGroupName) {
        String path = Encoder.path(rawGroupName, "NameColor");
        String value = getString(path, "gray");
        if ("gray".equalsIgnoreCase(value)) getDebugLog().debug("GroupsConfig.getNameColor: missing → gray @ " + path);
        return value;
    }

    public void setPrefix(String rawGroupName, String value) {
        String path = Encoder.path(rawGroupName, "Prefix");
        getDebugLog().info("GroupsConfig.setPrefix: " + path + " = " + value);
        set(path, value);
    }

    public void setSuffix(String rawGroupName, String value) {
        String path = Encoder.path(rawGroupName, "Suffix");
        getDebugLog().info("GroupsConfig.setSuffix: " + path + " = " + value);
        set(path, value);
    }

    public void setTabFormat(String rawGroupName, String value) {
        String path = Encoder.path(rawGroupName, "Tabformat");
        getDebugLog().info("GroupsConfig.setTabFormat: " + path + " = " + value);
        set(path, value);
    }

    public void setChatFormat(String rawGroupName, String value) {
        String path = Encoder.path(rawGroupName, "Chatformat");
        getDebugLog().info("GroupsConfig.setChatFormat: " + path + " = " + value);
        set(path, value);
    }

    public void setSortId(String rawGroupName, int sortId) {
        String path = Encoder.path(rawGroupName, "SortID");
        getDebugLog().info("GroupsConfig.setSortId: " + path + " = " + sortId);
        set(path, sortId);
    }

    public void setNameColor(String rawGroupName, String colorName) {
        String path = Encoder.path(rawGroupName, "NameColor");
        getDebugLog().info("GroupsConfig.setNameColor: " + path + " = " + colorName);
        set(path, colorName);
    }

    public void removeGroup(String group) {
        set(group, null);
    }

    public void ensureGroupDefaults(String rawGroupName,
                                    boolean autoAdd,
                                    boolean warn,
                                    Consumer<String> warnPrint) {
        String sectionKey = Encoder.key(rawGroupName);
        getDebugLog().info("GroupsConfig.ensureGroupDefaults: checking '" + rawGroupName + "' (section='" + sectionKey + "')");

        boolean exists = contains(sectionKey);
        if (!exists && !autoAdd) {
            String msg = "Group '" + rawGroupName + "' not found — skipping.";
            getDebugLog().warn("GroupsConfig.ensureGroupDefaults: " + msg);
            if (warn && warnPrint != null) warnPrint.accept(msg);
            return;
        }

        if (!exists) {
            set(sectionKey, new LinkedHashMap<String, Object>());
            String msg = "groups.yml: group '" + rawGroupName + "' was missing — creating with defaults.";
            getDebugLog().warn("GroupsConfig.ensureGroupDefaults: " + msg);
            if (warn && warnPrint != null) warnPrint.accept(msg);
        }

        setIfMissing(Encoder.path(rawGroupName, "Prefix"), "<gray>" + rawGroupName, warn, warnPrint, rawGroupName, "Prefix");
        setIfMissing(Encoder.path(rawGroupName, "Suffix"), "", warn, warnPrint, rawGroupName, "Suffix");
        setIfMissing(Encoder.path(rawGroupName, "Tabformat"), "<prefix> <dark_gray>- <gray><player>", warn, warnPrint, rawGroupName, "Tabformat");
        setIfMissing(Encoder.path(rawGroupName, "Chatformat"), "<prefix> <dark_gray>- <gray><player> <dark_gray>» <gray><message>", warn, warnPrint, rawGroupName, "Chatformat");

        boolean hasSortId = isSet(Encoder.path(rawGroupName, "SortID"));
        if (!hasSortId) {
            set(Encoder.path(rawGroupName, "SortID"), 999);
            String msg = "groups.yml: '" + rawGroupName + ".SortID' was missing — defaulting to 999.";
            getDebugLog().warn("GroupsConfig.ensureGroupDefaults: " + msg);
            if (warn && warnPrint != null) warnPrint.accept(msg);
        }

        setIfMissing(Encoder.path(rawGroupName, "NameColor"), "gray", warn, warnPrint, rawGroupName, "NameColor");
        getDebugLog().info("GroupsConfig.ensureGroupDefaults: ensured for '" + rawGroupName + "'");
    }

    private void setIfMissing(String dottedPath,
                              Object defaultValue,
                              boolean warn,
                              Consumer<String> warnPrint,
                              String groupName,
                              String keyName) {
        boolean present = isSet(dottedPath);
        if (present) return;

        set(dottedPath, defaultValue);
        String msg = "groups.yml: '" + groupName + "." + keyName + "' was missing — setting default: " + defaultValue;
        getDebugLog().warn("GroupsConfig.setIfMissing: " + msg);
        if (warn && warnPrint != null) warnPrint.accept(msg);
    }
}