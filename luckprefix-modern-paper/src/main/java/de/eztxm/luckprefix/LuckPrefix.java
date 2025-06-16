package de.eztxm.luckprefix;

import de.eztxm.ezlib.api.database.SQLConnection;
import de.eztxm.ezlib.database.MongoDBConnection;
import de.eztxm.luckprefix.command.LuckPrefixCommand;
import de.eztxm.luckprefix.common.util.UpdateChecker;
import de.eztxm.luckprefix.listener.ChatListener;
import de.eztxm.luckprefix.listener.GroupListener;
import de.eztxm.luckprefix.listener.JoinListener;
import de.eztxm.luckprefix.listener.QuitListener;
import de.eztxm.luckprefix.util.*;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class LuckPrefix extends JavaPlugin {
    @Getter
    private static LuckPrefix instance;
    @Getter
    private static boolean development = true;

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
        registry.registerCommand("de/eztxm/luckprefix", new LuckPrefixCommand());
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
                getLogger().warning("Newer version " + updateChecker.getCachedLatestVersion()
                        + " is available at https://modrinth.com/plugin/luckprefix");
            }
        }
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
