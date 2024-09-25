package de.eztxm.luckprefix;

import de.eztxm.api.database.SQLConnection;
import de.eztxm.database.MongoDBConnection;
import de.eztxm.luckprefix.command.LuckPrefixCommand;
import de.eztxm.luckprefix.listener.ChatListener;
import de.eztxm.luckprefix.listener.GroupListener;
import de.eztxm.luckprefix.listener.JoinListener;
import de.eztxm.luckprefix.listener.QuitListener;
import de.eztxm.luckprefix.util.*;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class LuckPrefix extends JavaPlugin {
    @Getter
    private static LuckPrefix instance;

    private String prefix;
    private ConfigManager databaseFile;
    private ConfigManager groupsFile;
    private MongoDBConnection mongoDBConnection;
    private SQLConnection sqlConnection;
    private BukkitAudiences adventure;
    private LuckPerms luckPerms;
    private Registry registry;
    private SQLDatabaseManager sqlDatabaseManager;
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
            if (getDatabaseFile().getValue("Database.Type").asString().equalsIgnoreCase("mongodb")) {
                mongoDBConnection = MongoDBManager.createMongoDBConnection();
            } else {
                sqlConnection = SQLDatabaseManager.createSQLDatabaseConnection();
                sqlDatabaseManager = new SQLDatabaseManager(sqlConnection);
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
        groupManager = new GroupManager();
        groupListener = new GroupListener();
        groupListener.onCreateGroup();
        groupListener.onDeleteGroup();
        groupManager.loadGroups();
        updateChecker = new UpdateChecker(getDescription().getVersion());
        if (!updateChecker.latestVersion()) {
            getLogger().warning("Newer version " + updateChecker.getCachedLatestVersion() + " is available at https://modrinth.com/plugin/luckprefix");
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
