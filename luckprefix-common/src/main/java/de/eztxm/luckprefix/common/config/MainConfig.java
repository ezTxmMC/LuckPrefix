package de.eztxm.luckprefix.common.config;

import de.eztxm.luckprefix.common.logging.DebugLog;

import java.nio.file.Path;
import java.util.List;

public final class MainConfig extends AbstractConfig {

    private final String pluginVersion;

    public MainConfig(Path filePath, DebugLog debugLog, String pluginVersion) {
        super(filePath, debugLog);
        this.pluginVersion = pluginVersion == null ? "unknown" : pluginVersion;
        getDebugLog().info("MainConfig: constructed, version=" + this.pluginVersion);
    }

    @Override
    protected void defineDefaults() {
        getDebugLog().info("MainConfig.defineDefaults: applying");

        setHeaderComments(List.of(
                " _               _    ____            __ _",
                "| |   _   _  ___| | _|  _ \\ _ __ ___ / _(_)_  __",
                "| |  | | | |/ __| |/ / |_) | '__/ _ \\ |_| \\ \\/ /",
                "| |__| |_| | (__|   <|  __/| | |  __/  _| |>  <",
                "|_____\\__,_|\\___|_|\\_\\_|   |_|  \\___|_| |_/_/\\_\\",
                "Modern " + pluginVersion + " | by ezTxmMC",
                "",
                "This plugin supports MiniMessage and legacy '&' color codes."
        ));

        addDefault("ColoredPermission", "luckprefix.coloredmessages");
        addDefault("AutoReloadConfig.Enabled", false);
        addDefault("AutoReloadConfig.Interval", 10);
        addDefault("UpdateTime", 5);
        addDefault("ShowNameTags", true);
        addDefault("UpdateAvailableMessage", true);
        addDefault("Warning-If-Group-Can-Not-Loaded", true);
        addDefault("Auto-Add-Group", true);

        addDefault("Logging.ConsoleEnabled", true);
        addDefault("Logging.Debug.Enabled", true);
        addDefault("Logging.Debug.File", "luckprefix-debug.log");

        saveDefaults();
        getDebugLog().debug("MainConfig.defineDefaults: defaults saved");

        setComments("ColoredPermission", List.of(
                "Permission required to use color codes in chat"
        ));
        setComments("AutoReloadConfig", List.of("Automatically reload of the config"));
        setComments("AutoReloadConfig.Enabled", List.of("If true it automatically reloads the config after the interval"));
        setComments("AutoReloadConfig.Interval", List.of("Interval in seconds to reload the config"));
        setComments("UpdateTime", List.of("Time in seconds to refresh prefixes"));
        setComments("ShowNameTags", List.of("Shows the nametag above the players"));
        setComments("UpdateAvailableMessage", List.of("Show message when a new update is available"));
        setComments("Warning-If-Group-Can-Not-Loaded", List.of("Warn for each group missing in groups.yml"));
        setComments("Auto-Add-Group", List.of(
                "If true it will automatically insert not existing groups into groups.yml or the database",
                "ONLY DISABLE IF YOU KNOW WHAT YOU ARE DOING!"
        ));
        setComments("Logging", List.of("Logging settings"));
        setComments("Logging.ConsoleEnabled", List.of("If true, prints warnings/info to console"));
        setComments("Logging.Debug.Enabled", List.of("Deprecated toggle; debug file logger is always active"));
        setComments("Logging.Debug.File", List.of("Debug log file path"));

        saveComments();
        getDebugLog().debug("MainConfig.defineDefaults: comments saved");
    }

    public String getColoredPermission() {
        String value = getString("ColoredPermission", "luckprefix.coloredmessages");
        getDebugLog().debug("MainConfig.getColoredPermission -> " + value);
        return value;
    }

    public boolean isAutoReloadEnabled() {
        boolean value = getBoolean("AutoReloadConfig.Enabled", false);
        getDebugLog().debug("MainConfig.isAutoReloadEnabled -> " + value);
        return value;
    }

    public int getAutoReloadIntervalSeconds() {
        int value = getInt("AutoReloadConfig.Interval", 10);
        getDebugLog().debug("MainConfig.getAutoReloadIntervalSeconds -> " + value);
        return value;
    }

    public int getUpdateTimeSeconds() {
        int value = getInt("UpdateTime", 5);
        getDebugLog().debug("MainConfig.getUpdateTimeSeconds -> " + value);
        return value;
    }

    public boolean isShowNameTags() {
        boolean value = getBoolean("ShowNameTags", true);
        getDebugLog().debug("MainConfig.isShowNameTags -> " + value);
        return value;
    }

    public boolean isUpdateAvailableMessageEnabled() {
        boolean value = getBoolean("UpdateAvailableMessage", true);
        getDebugLog().debug("MainConfig.isUpdateAvailableMessageEnabled -> " + value);
        return value;
    }

    public boolean isWarnIfGroupCannotBeLoaded() {
        boolean value = getBoolean("Warning-If-Group-Can-Not-Loaded", true);
        getDebugLog().debug("MainConfig.isWarnIfGroupCannotBeLoaded -> " + value);
        return value;
    }

    public boolean isAutoAddGroupEnabled() {
        boolean value = getBoolean("Auto-Add-Group", true);
        getDebugLog().debug("MainConfig.isAutoAddGroupEnabled -> " + value);
        return value;
    }

    public boolean isConsoleLoggingEnabled() {
        boolean value = getBoolean("Logging.ConsoleEnabled", true);
        getDebugLog().debug("MainConfig.isConsoleLoggingEnabled -> " + value);
        return value;
    }

    public boolean isDebugLoggingFlagEnabled() {
        boolean value = getBoolean("Logging.Debug.Enabled", true);
        getDebugLog().debug("MainConfig.isDebugLoggingFlagEnabled -> " + value);
        return value;
    }

    public String getDebugLogFile() {
        String value = getString("Logging.Debug.File", "luckprefix-debug.log");
        getDebugLog().debug("MainConfig.getDebugLogFile -> " + value);
        return value;
    }
}