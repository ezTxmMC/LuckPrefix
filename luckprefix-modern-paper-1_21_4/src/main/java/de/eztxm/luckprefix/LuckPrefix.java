package de.eztxm.luckprefix;

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
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.ConfigUtil;
import de.eztxm.luckprefix.util.GroupManager;
import de.eztxm.luckprefix.util.MongoDBManager;
import de.eztxm.luckprefix.util.PlayerManager;
import de.eztxm.luckprefix.util.SQLDatabaseManager;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

@Getter
public final class LuckPrefix extends JavaPlugin {

    @Getter
    private static LuckPrefix instance;
    @Getter
    private static final boolean development = true;

    private String prefix;
    private ConfigManager databaseFile;
    private ConfigManager groupsFile;
    private MongoDBConnection mongoDBConnection;
    private SQLConnection sqlConnection;
    private BukkitAudiences adventure;
    private LuckPerms luckPerms;
    private Registry registry;
    private SQLDatabaseManager sqlDatabaseManager;
    private MongoDBManager mongoDBManager;
    private PlayerManager playerManager;
    private GroupManager groupManager;
    private GroupListener groupListener;
    private UpdateChecker updateChecker;
    private BukkitTask autoReloadConfigTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        prefix = "<#77ef77>LuckPrefix <dark_gray>| <gray>";
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
        adventure = BukkitAudiences.create(instance);
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
        updateChecker = new UpdateChecker(Bukkit.getVersion());
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
        StringBuilder sb = new StringBuilder();
        sb.append("LuckPrefix has been enabled successfully!\n");
        sb.append("Version: ").append(getDescription().getVersion()).append("\n");
        sb.append("Author: ezTxmMC\n");
        sb.append("Website: https://modrinth.com/plugin/luckprefix\n");
        sb.append("Discord: https://eztxm.de/dc\n");
        sb.append("GitHub: https://github.com/ezTxmMC/LuckPrefix\n");
        sb.append("LuckPrefix is running in ").append(development ? "development" : "production")
                .append(" mode.\n");
        if (getDatabaseFile().getValue("Database.Enabled").asBoolean()) {
            sb.append("Database connection established successfully.\n");
        } else {
            sb.append("Database connection is disabled, using groups.yml configuration.\n");
        }
        sb.append("Groups are loaded from ")
                .append(getDatabaseFile().getValue("Database.Enabled").asBoolean() ? "database." : "groups.yml file.\n");
        sb.append("You can use /luckprefix command to manage groups and prefixes.");
        getLogger().info(sb.toString());
    }

    @Override
    public void onDisable() {
        instance = null;
        registry = null;
        playerManager = null;
        groupManager = null;
        groupListener = null;
    }
}
