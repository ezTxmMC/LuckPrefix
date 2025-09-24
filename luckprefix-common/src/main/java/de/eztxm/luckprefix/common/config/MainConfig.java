package de.eztxm.luckprefix.common.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MainConfig extends AbstractConfig {
    private final String pluginVersion;

    public MainConfig(Path filePath, String pluginVersion) {
        super(filePath);
        this.pluginVersion = pluginVersion;
    }

    @Override
    protected void defineDefaults() {
        addDefault("ColoredPermission", "luckprefix.coloredmessages");

        addDefault("AutoReloadConfig.Enabled", false);
        addDefault("AutoReloadConfig.Interval", 10);

        addDefault("UpdateTime", 5);
        addDefault("ShowNameTags", true);
        addDefault("UpdateAvailableMessage", true);

        addDefault("Warning-If-Group-Can-Not-Loaded", true);
        addDefault("Auto-Add-Group", true);

        addDefault("Logging.ConsoleEnabled", false);
        addDefault("Logging.Debug.Enabled", false);
        addDefault("Logging.Debug.File", "luckprefix-debug.log");

        saveDefaults();

        List<String> banner = new ArrayList<>(List.of(
                " _               _    ____            __ _",
                "| |   _   _  ___| | _|  _ \\ _ __ ___ / _(_)_  __",
                "| |  | | | |/ __| |/ / |_) | '__/ _ \\ |_| \\ \\/ /",
                "| |__| |_| | (__|   <|  __/| | |  __/  _| |>  <",
                "|_____\\__,_|\\___|_|\\_\\_|   |_|  \\___|_| |_/_/\\_\\",
                "Modern %s | by ezTxmMC".formatted(pluginVersion),
                "",
                "Permission to write with color codes in chat"
        ));
        setComments("ColoredPermission", banner);
        setComments("AutoReloadConfig", List.of("Automatically reload configuration files"));
        setComments("AutoReloadConfig.Enabled", List.of("If true, automatically reload after the interval"));
        setComments("AutoReloadConfig.Interval", List.of("Interval in seconds for auto-reload"));
        setComments("UpdateTime", List.of("Interval in seconds to refresh prefixes"));
        setComments("ShowNameTags", List.of("Show the nametag above players"));
        setComments("UpdateAvailableMessage", List.of("Show a message if an update is available"));
        setComments("Warning-If-Group-Can-Not-Loaded", List.of("Warn once per missing group in groups.yml"));
        setComments("Auto-Add-Group", List.of(
                "If true, non-existing groups are inserted into groups.yml or DB.",
                "ONLY DISABLE IF YOU KNOW WHAT YOU ARE DOING!"
        ));
        setComments("Logging", List.of("Logging settings"));
        setComments("Logging.ConsoleEnabled", List.of("If true, logs are printed to console"));
        setComments("Logging.Debug", List.of("Debug file logging"));
        setComments("Logging.Debug.Enabled", List.of("If true, writes a debug log file"));
        setComments("Logging.Debug.File", List.of("Debug log file path (relative to plugin folder)"));

        saveComments();
    }

    public String getColoredPermission() {
        return getString("ColoredPermission", "luckprefix.coloredmessages");
    }

    public void setColoredPermission(String permission) {
        set("ColoredPermission", permission);
    }

    public boolean isAutoReloadEnabled() {
        return getBoolean("AutoReloadConfig.Enabled", false);
    }

    public void setAutoReloadEnabled(boolean enabled) {
        set("AutoReloadConfig.Enabled", enabled);
    }

    public int getAutoReloadIntervalSeconds() {
        return getInt("AutoReloadConfig.Interval", 10);
    }

    public void setAutoReloadIntervalSeconds(int seconds) {
        set("AutoReloadConfig.Interval", seconds);
    }

    public int getUpdateTimeSeconds() {
        return getInt("UpdateTime", 5);
    }

    public void setUpdateTimeSeconds(int seconds) {
        set("UpdateTime", seconds);
    }

    public boolean isShowNameTagsEnabled() {
        return getBoolean("ShowNameTags", true);
    }

    public void setShowNameTagsEnabled(boolean enabled) {
        set("ShowNameTags", enabled);
    }

    public boolean isUpdateAvailableMessageEnabled() {
        return getBoolean("UpdateAvailableMessage", true);
    }

    public void setUpdateAvailableMessageEnabled(boolean enabled) {
        set("UpdateAvailableMessage", enabled);
    }

    public boolean isWarnIfGroupCannotBeLoaded() {
        return getBoolean("Warning-If-Group-Can-Not-Loaded", true);
    }

    public void setWarnIfGroupCannotBeLoaded(boolean enabled) {
        set("Warning-If-Group-Can-Not-Loaded", enabled);
    }

    public boolean isAutoAddGroupEnabled() {
        return getBoolean("Auto-Add-Group", true);
    }

    public void setAutoAddGroupEnabled(boolean enabled) {
        set("Auto-Add-Group", enabled);
    }

    public boolean isConsoleLoggingEnabled() {
        return getBoolean("Logging.ConsoleEnabled", false);
    }

    public void setConsoleLoggingEnabled(boolean enabled) {
        set("Logging.ConsoleEnabled", enabled);
    }

    public boolean isDebugLoggingEnabled() {
        return getBoolean("Logging.Debug.Enabled", false);
    }

    public void setDebugLoggingEnabled(boolean enabled) {
        set("Logging.Debug.Enabled", enabled);
    }

    public String getDebugLogFile() {
        return getString("Logging.Debug.File", "luckprefix-debug.log");
    }

    public void setDebugLogFile(String fileName) {
        set("Logging.Debug.File", fileName);
    }
}