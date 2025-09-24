package de.eztxm.luckprefix;

import de.eztxm.ezlib.database.MongoDBConnection;
import de.eztxm.luckprefix.command.LuckPrefixCommand;
import de.eztxm.luckprefix.common.config.*;
import de.eztxm.luckprefix.common.logging.DebugLog;
import de.eztxm.luckprefix.common.util.UpdateChecker;
import de.eztxm.luckprefix.depend.LuckPrefixPlaceholderExtension;
import de.eztxm.luckprefix.listener.ChatListener;
import de.eztxm.luckprefix.listener.GroupListener;
import de.eztxm.luckprefix.listener.JoinListener;
import de.eztxm.luckprefix.listener.QuitListener;
import de.eztxm.luckprefix.util.DependUtil;
import de.eztxm.luckprefix.util.GroupManager;
import de.eztxm.luckprefix.util.PlayerManager;
import de.eztxm.luckprefix.util.Text;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public final class LuckPrefix extends JavaPlugin {

    @Getter
    private static final boolean development = true;
    @Getter
    private static LuckPrefix instance;
    @Getter
    private static boolean leafCompatibility = false;

    private DebugLog debugLog;

    private String prefix;
    private DependUtil dependUtil;
    private ConfigService configService;
    private ConfigWatcher configWatcher;
    private MongoDBConnection mongoDBConnection;
    private LuckPerms luckPerms;
    private Registry registry;
    private PlayerManager playerManager;
    private GroupManager groupManager;
    private GroupListener groupListener;
    private UpdateChecker updateChecker;
    private BukkitTask autoReloadConfigTask;
    private Metrics metrics;

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable() {
        setupLogger();
        checkCompatibility();
        saveDefaultConfig();
        instance = this;
        debugLog.info("Initializing LuckPrefix...");
        prefix = "<gradient:#42EC63:#66EC82>LuckPrefix <dark_gray>| <gray>";
        dependUtil = new DependUtil(this);
        if (!dependUtil.isLuckPermsEnabled()) {
            this.getServer().sendMessage(new Text("<#ff2222>LuckPerms can't be found. Disabling LuckPrefix...").prefixMiniMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupConfigs();
        luckPerms = LuckPermsProvider.get();
        registry = new Registry(instance);
        registry.registerCommand("luckprefix", new LuckPrefixCommand());
        registry.registerListener(new JoinListener());
        registry.registerListener(new QuitListener());
        registry.registerListener(new ChatListener());
        playerManager = new PlayerManager();
        groupManager = new GroupManager(instance);
        groupListener = new GroupListener(this.luckPerms, this.groupManager, this.playerManager);
        groupListener.onCreateGroup();
        groupListener.onDeleteGroup();
        groupListener.onUpdateGroup();
        groupListener.onUpdateUserGroup();
        groupManager.loadGroups();
        if (dependUtil.isPlaceholderAPIEnabled()) {
            new LuckPrefixPlaceholderExtension(this.getPluginMeta()).register();
            this.getServer().sendMessage(new Text("<#33ffff>PlaceholderAPI <gray>was detected successfully.").prefixMiniMessage());
        }
        updateChecker = new UpdateChecker(this.getPluginMeta().getVersion());
        if (!development) {
            if (!updateChecker.latestVersion()) {
                String message = "Newer version " + updateChecker.getCachedLatestVersion()
                        + " is available at https://modrinth.com/plugin/luckprefix";
                getLogger().warning(message);
            }
        }
        if (isLeafCompatibility()) {
            getLogger().info("LuckPrefix is running in Leaf compatibility mode.");
        }
        MainConfig mainConfig = configService.of(MainConfig.class);
        metrics = new Metrics(instance, 27277);
        metrics.addCustomChart(new SimplePie("used_groups", () -> String.valueOf(groupManager.getLoadedGroups().size())));
        metrics.addCustomChart(new SimplePie("auto_reload", () -> String.valueOf(mainConfig.isAutoReloadEnabled())));
        metrics.addCustomChart(new SimplePie("console_logging", () -> String.valueOf(mainConfig.isConsoleLoggingEnabled())));
        metrics.addCustomChart(new SimplePie("print_warnings", () -> String.valueOf(mainConfig.isPrintWarningsEnabled())));
        metrics.addCustomChart(new SimplePie("debug_logging_flag", () -> String.valueOf(mainConfig.isDebugLoggingFlagEnabled())));
        metrics.addCustomChart(new SimplePie("show_nametags", () -> String.valueOf(mainConfig.isShowNameTags())));
    }

    private void setupConfigs() {
        this.configService = new ConfigService();

        Path dataFolderPath = getDataFolder().toPath();
        Path configPath = dataFolderPath.resolve("config.yml");
        Path databasePath = dataFolderPath.resolve("database.yml");
        Path groupsPath = dataFolderPath.resolve("groups.yml");
        String pluginVersion = getDescription().getVersion();

        configService.register(MainConfig.class, configPath, path -> new MainConfig(path, debugLog, pluginVersion));
        configService.register(DatabaseConfig.class, databasePath, path -> new DatabaseConfig(path, debugLog));
        configService.register(GroupsConfig.class, groupsPath, path -> new GroupsConfig(path, debugLog));

        MainConfig config = configService.of(MainConfig.class);
        startConfigWatcher(config);
    }

    public void startConfigWatcher(MainConfig config) {
        if (config.isAutoReloadEnabled()) {
            this.configWatcher = new ConfigWatcher(getDataPath().toFile(), path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                switch (fileName) {
                    case "config.yml" -> {
                        configService.reload(MainConfig.class);
                        Bukkit.getScheduler().runTask(this, () -> groupManager.reloadAllFromConfigs());
                    }
                    case "database.yml" -> {
                        configService.reload(DatabaseConfig.class);
                        // TODO: Reload all Group Caches
                    }
                    case "groups.yml" -> {
                        configService.reload(GroupsConfig.class);
                        Bukkit.getScheduler().runTask(this, () -> groupManager.reloadAllFromConfigs());
                    }
                    default -> {
                        getLogger().warning("Unknown config file: " + fileName);
                        return false;
                    }
                }
                getLogger().info("Reloading %s file".formatted(fileName));
                return true;
            });
            this.configWatcher.start();
        }
    }

    private void setupLogger() {
        debugLog = new DebugLog(getDataFolder().toPath().resolve("debug.log"), 1_000_000L);
    }

    private void checkCompatibility() {
        String bukkitVersion = Bukkit.getServer().getBukkitVersion();
        String brand = (Bukkit.getName() + " " + bukkitVersion).toLowerCase();
        leafCompatibility = brand.contains("leaf");
        debugLog.info("LuckPrefix compatibility has been detected in " + brand);
    }

    @Override
    public void onDisable() {
        instance = null;
        registry = null;
        playerManager = null;
        groupManager = null;
        groupListener = null;
        updateChecker = null;
        configService = null;
        if (configWatcher != null) {
            configWatcher.stop();
        }
        configWatcher = null;
        mongoDBConnection = null;
        luckPerms = null;
        autoReloadConfigTask = null;
        metrics.shutdown();
        metrics = null;
    }
}
