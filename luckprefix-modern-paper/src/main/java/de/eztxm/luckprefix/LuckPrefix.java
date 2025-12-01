package de.eztxm.luckprefix;

import de.eztxm.ezlib.database.MongoDBConnection;
import de.eztxm.luckprefix.api.ILuckPrefixAPI;
import de.eztxm.luckprefix.api.config.AbstractConfig;
import de.eztxm.luckprefix.api.event.IGroupListener;
import de.eztxm.luckprefix.api.logging.IDebugLog;
import de.eztxm.luckprefix.api.manager.IGroupManager;
import de.eztxm.luckprefix.api.manager.IPlayerManager;
import de.eztxm.luckprefix.command.LuckPrefixCommand;
import de.eztxm.luckprefix.common.config.*;
import de.eztxm.luckprefix.common.logging.DebugLog;
import de.eztxm.luckprefix.common.util.UpdateChecker;
import de.eztxm.luckprefix.depend.LuckPrefixPlaceholderExtension;
import de.eztxm.luckprefix.manager.GroupManager;
import de.eztxm.luckprefix.listener.ChatListener;
import de.eztxm.luckprefix.listener.GroupListener;
import de.eztxm.luckprefix.listener.JoinListener;
import de.eztxm.luckprefix.listener.QuitListener;
import de.eztxm.luckprefix.util.DependUtil;
import de.eztxm.luckprefix.util.LuckPlayer;
import de.eztxm.luckprefix.manager.PlayerManager;
import de.eztxm.luckprefix.util.Text;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.nio.file.Path;

@Getter
public final class LuckPrefix extends JavaPlugin implements ILuckPrefixAPI {

    @Getter
    private static final boolean development = true;
    @Getter
    private static LuckPrefix instance;

    private static String serverBrand = "";

    private IDebugLog debugLog;

    private String prefix;
    private DependUtil dependUtil;
    private ConfigService configService;
    private ConfigWatcher configWatcher;
    private MongoDBConnection mongoDBConnection;
    private LuckPerms luckPerms;
    private Registry registry;
    private IPlayerManager playerManager;
    private IGroupManager groupManager;
    private IGroupListener groupListener;
    private UpdateChecker updateChecker;
    private BukkitTask autoReloadConfigTask;
    private Metrics metrics;

    private BukkitTask tabUpdateTask;

    @Override
    public void onLoad() {
        this.loaded();
    }

    @Override
    public void onEnable() {
        this.enabled();
    }

    @Override
    public void onDisable() {
        this.disabled();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void loaded() {
        setupLogger();
        instance = this;
        debugLog.info("Initializing LuckPrefix...");
        prefix = "<gradient:#42EC63:#66EC82>LuckPrefix</gradient> <dark_gray>| <gray>";
        dependUtil = new DependUtil(this);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void enabled() {
        fetchBrand();
        if (serverBrand.equalsIgnoreCase("spigot") || serverBrand.equalsIgnoreCase("bukkit")) {
            getServer().sendMessage(new Text("<#ff2222>LuckPrefix Paper-based is not compatible with %s. Use another version of LuckPrefix instead if available.".formatted(serverBrand)).prefixMiniMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().sendMessage(new Text("<gradient:#42EC63:#66EC82>LuckPrefix</gradient> <gray>is running on <aqua>%s".formatted(serverBrand)).miniMessage());
        getServer().sendMessage(new Text("<gray>Version: <aqua>%s".formatted(this.getPluginMeta().getVersion())).miniMessage());
        getServer().sendMessage(new Text("<gray>by %s".formatted(String.join(", ", this.getPluginMeta().getAuthors()))).miniMessage());
        setupConfigs();
        if (!dependUtil.isLuckPermsEnabled()) {
            this.getServer().sendMessage(new Text("<#ff2222>LuckPerms can't be found. Disabling LuckPrefix...").prefixMiniMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        MainConfig mainConfig = configService.of(MainConfig.class);
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
        updateGroups();
        if (dependUtil.isPlaceholderAPIEnabled()) {
            new LuckPrefixPlaceholderExtension(this.getPluginMeta()).register();
            this.getServer().sendMessage(new Text("<#33ffff>PlaceholderAPI <gray>was detected successfully.").prefixMiniMessage());
        }
        updateChecker = new UpdateChecker(mainConfig.getUpdateChannel(), this.getPluginMeta().getVersion(), debugLog);
        if (!updateChecker.isLatestVersion(development)) {
            String message = "Newer version " + updateChecker.getCachedLatestVersion()
                    + " is available at https://modrinth.com/plugin/luckprefix";
            getLogger().warning(message);
        }
        setupMetrics();
    }

    @Override
    public void disabled() {
        instance = null;
        registry = null;
        playerManager = null;
        groupManager = null;
        groupListener = null;
        if(tabUpdateTask != null) {
            tabUpdateTask.cancel();
            tabUpdateTask = null;
        }
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

    @Override
    public void startConfigWatcher(AbstractConfig abstractConfig) {
        MainConfig config = (MainConfig) abstractConfig;
        if (config.isAutoReloadEnabled()) {
            this.configWatcher = new ConfigWatcher(getDataPath().toFile(), path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                switch (fileName) {
                    case "config.yml" -> {
                        configService.reload(MainConfig.class);
                        Bukkit.getScheduler().runTask(this, () -> groupManager.reloadFromConfig());
                    }
                    case "database.yml" -> {
                        configService.reload(DatabaseConfig.class);
                        // TODO: Reload all Group Caches
                    }
                    case "groups.yml" -> {
                        configService.reload(GroupsConfig.class);
                        Bukkit.getScheduler().runTask(this, () -> groupManager.reloadFromConfig());
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

    private void fetchBrand() {
        String bukkitVersion = Bukkit.getServer().getBukkitVersion();
        String brand = (Bukkit.getName() + " " + bukkitVersion).toLowerCase();
        if (brand.toLowerCase().contains("bukkit")) {
            serverBrand = "Bukkit";
            return;
        }
        if (brand.toLowerCase().contains("spigot")) {
            serverBrand = "Spigot";
            return;
        }
        if (brand.toLowerCase().contains("paper")) {
            serverBrand = "Paper";
            return;
        }
        if (brand.toLowerCase().contains("purpur")) {
            serverBrand = "Purpur";
            return;
        }
        if (brand.toLowerCase().contains("leaf")) {
            serverBrand = "Leaf";
            return;
        }
        serverBrand = "Unknown";
        debugLog.info("LuckPrefix compatibility has been detected in " + brand);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void setupConfigs() {
        this.configService = new ConfigService();

        Path dataFolderPath = getDataFolder().toPath();
        Path configPath = dataFolderPath.resolve("config.yml");
        Path databasePath = dataFolderPath.resolve("database.yml");
        Path groupsPath = dataFolderPath.resolve("groups.yml");
        String pluginVersion = getPluginMeta().getVersion();

        configService.register(MainConfig.class, configPath, path -> new MainConfig(path, debugLog, pluginVersion));
        configService.register(DatabaseConfig.class, databasePath, path -> new DatabaseConfig(path, debugLog));
        configService.register(GroupsConfig.class, groupsPath, path -> new GroupsConfig(path, debugLog));

        MainConfig config = configService.of(MainConfig.class);
        startConfigWatcher(config);
    }

    private void updateGroups() {
        long periodTicks = getConfigService().of(MainConfig.class).getUpdateTime();
        if(periodTicks < 5L) periodTicks = 5L;
        this.tabUpdateTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                for(Player viewer : Bukkit.getOnlinePlayers()) {
                    LuckPlayer luckPlayer = new LuckPlayer(viewer);
                    getGroupManager().setGroups(luckPlayer, luckPlayer.getScoreboard());
                }
            } catch (Exception exception) {
                getDebugLog().error("tabUpdateTask failed", exception);
            }
        }, 1L, periodTicks);
    }

    private void setupMetrics() {
        MainConfig mainConfig = configService.of(MainConfig.class);
        metrics = new Metrics(instance, 27277);
        metrics.addCustomChart(new SimplePie("used_groups", () -> String.valueOf(groupManager.getLoadedGroups().size())));
        metrics.addCustomChart(new SimplePie("auto_reload", () -> String.valueOf(mainConfig.isAutoReloadEnabled())));
        metrics.addCustomChart(new SimplePie("console_logging", () -> String.valueOf(mainConfig.isConsoleLoggingEnabled())));
        metrics.addCustomChart(new SimplePie("print_warnings", () -> String.valueOf(mainConfig.isPrintWarningsEnabled())));
        metrics.addCustomChart(new SimplePie("debug_logging_flag", () -> String.valueOf(mainConfig.isDebugLoggingFlagEnabled())));
        metrics.addCustomChart(new SimplePie("show_nametags", () -> String.valueOf(mainConfig.isShowNameTags())));
    }
}
