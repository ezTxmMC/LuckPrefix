package de.eztxm.luckprefix;

import de.eztxm.ezlib.database.MongoDBConnection;
import de.eztxm.luckprefix.command.LuckPrefixCommand;
import de.eztxm.luckprefix.common.util.UpdateChecker;
import de.eztxm.luckprefix.depend.LuckPrefixPlaceholderExtension;
import de.eztxm.luckprefix.listener.ChatListener;
import de.eztxm.luckprefix.listener.GroupListener;
import de.eztxm.luckprefix.listener.JoinListener;
import de.eztxm.luckprefix.listener.QuitListener;
import de.eztxm.luckprefix.util.*;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

@Getter
public final class LuckPrefix extends JavaPlugin {

    @Getter
    private static LuckPrefix instance;
    @Getter
    private static final boolean development = true;
    @Getter
    private static boolean leafCompatibility = false;

    private String prefix;
    private DependUtil dependUtil;
    private ConfigManager databaseFile;
    private ConfigManager groupsFile;
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
        checkCompatibility();
        saveDefaultConfig();
        instance = this;
        prefix = "<gradient:#42EC63:#66EC82>LuckPrefix <dark_gray>| <gray>";
        dependUtil = new DependUtil(this);
        if (!dependUtil.isLuckPermsEnabled()) {
            this.getServer().sendMessage(new Text("<#ff2222>LuckPerms can't be found. Disabling LuckPrefix...").prefixMiniMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        databaseFile = ConfigUtil.addDatabaseDefault("database.yml");
        groupsFile = ConfigUtil.addGroupsDefault("groups.yml");
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
        if (getConfig().getBoolean("Auto-Reload-Config.Enabled")) {
            autoReloadConfigTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                try {
                    if (getConfig().getBoolean("Auto-Reload-Config.Enabled")) {
                        getLogger().info("Reloading configuration files...");
                        databaseFile.reloadConfig();
                        groupsFile.reloadConfig();
                        getConfig().options().copyDefaults(true);
                        saveDefaultConfig();
                        groupManager.loadGroups();
                        getLogger().info("Configuration files reloaded successfully.");
                    }
                } catch (Exception e) {
                    String message = "Error while reloading configuration files: " + e.getMessage();
                    getLogger().severe(message);
                }
            }, 0L, getConfig().getLong("Auto-Reload-Config.Interval") * 20L);
        }
        if (isLeafCompatibility()) {
            getLogger().info("LuckPrefix is running in Leaf Compatibility mode.");
        }
        metrics = new Metrics(instance, 27277);
    }

    private void checkCompatibility() {
        String bukkitVersion = Bukkit.getServer().getBukkitVersion();
        String brand = (Bukkit.getName() + " " + bukkitVersion).toLowerCase();
        leafCompatibility = brand.contains("leaf");
    }

    @Override
    public void onDisable() {
        instance = null;
        registry = null;
        playerManager = null;
        groupManager = null;
        groupListener = null;
        updateChecker = null;
        groupsFile = null;
        mongoDBConnection = null;
        luckPerms = null;
        databaseFile = null;
        autoReloadConfigTask = null;
        metrics.shutdown();
        metrics = null;
    }
}
