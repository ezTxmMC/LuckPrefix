package de.eztxm.luckprefix;

import de.eztxm.luckprefix.depend.LuckPrefixPlaceholderExtension;
import de.eztxm.luckprefix.util.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import de.eztxm.ezlib.api.database.SQLConnection;
import de.eztxm.ezlib.database.MongoDBConnection;
import de.eztxm.luckprefix.command.LuckPrefixCommand;
import de.eztxm.luckprefix.common.util.UpdateChecker;
import de.eztxm.luckprefix.listener.ChatListener;
import de.eztxm.luckprefix.listener.GroupListener;
import de.eztxm.luckprefix.listener.JoinListener;
import de.eztxm.luckprefix.listener.QuitListener;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

@Getter
public final class LuckPrefix extends JavaPlugin {

    @Getter
    private static LuckPrefix instance;
    @Getter
    private static final boolean development = true;

    private String prefix;
    private DependUtil dependUtil;
    private ConfigManager databaseFile;
    private ConfigManager groupsFile;
    private MongoDBConnection mongoDBConnection;
    private SQLConnection sqlConnection;
    private LuckPerms luckPerms;
    private Registry registry;
    private SQLDatabaseManager sqlDatabaseManager;
    private MongoDBManager mongoDBManager;
    private PlayerManager playerManager;
    private GroupManager groupManager;
    private GroupListener groupListener;
    private UpdateChecker updateChecker;
    private BukkitTask autoReloadConfigTask;

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        prefix = "<#77ef77>LuckPrefix <dark_gray>| <gray>";
        dependUtil = new DependUtil(this);
        if (!dependUtil.isLuckPermsEnabled()) {
            this.getServer().sendMessage(new Text("<#ff2222>LuckPerms can't be found. Disabling LuckPrefix...").prefixMiniMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        databaseFile = ConfigUtil.addDatabaseDefault("database.yml");
        groupsFile = ConfigUtil.addGroupsDefault("groups.yml");
        if (getDatabaseFile().getValue("Database.Enabled").asBoolean()) {
            if (!development) {
                this.getLogger().warning(
                        "Database connections currently not work correctly. Please use groups.yml configuration and disable database.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            switch (getDatabaseFile().getValue("Database.Type").asString().toUpperCase()) {
                case "MARIADB", "SQLITE" -> {
                    sqlConnection = SQLDatabaseManager
                            .createSQLDatabaseConnection(getDatabaseFile().getConfiguration());
                    sqlDatabaseManager = new SQLDatabaseManager(sqlConnection);
                }
                case "MONGODB" -> {
                    mongoDBConnection = MongoDBManager.createMongoDBConnection(getDatabaseFile().getConfiguration());
                    mongoDBManager = new MongoDBManager(mongoDBConnection);
                }
            }
        }
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
        sqlConnection = null;
        sqlDatabaseManager = null;
        mongoDBManager = null;
        luckPerms = null;
        databaseFile = null;
        autoReloadConfigTask = null;
    }
}
