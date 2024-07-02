package de.eztxm.luckprefix;

import de.eztxm.api.database.SQLConnection;
import de.eztxm.luckprefix.command.LuckPrefixCommand;
import de.eztxm.luckprefix.listener.ChatListener;
import de.eztxm.luckprefix.listener.GroupListener;
import de.eztxm.luckprefix.listener.JoinListener;
import de.eztxm.luckprefix.listener.QuitListener;
import de.eztxm.luckprefix.util.DatabaseManager;
import de.eztxm.luckprefix.util.FileManager;
import de.eztxm.luckprefix.util.GroupManager;
import de.eztxm.luckprefix.util.PlayerManager;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class LuckPrefix extends JavaPlugin {
    @Getter
    private static LuckPrefix instance;

    private String prefix;
    private FileManager databaseFile;
    private FileManager groupsFile;
    private SQLConnection connection;
    private BukkitAudiences adventure;
    private LuckPerms luckPerms;
    private Registry registry;
    private DatabaseManager databaseManager;
    private PlayerManager playerManager;
    private GroupManager groupManager;
    private GroupListener groupListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        prefix = "<#77ef77>LuckPrefix <dark_gray>| <gray>";
        databaseFile = new FileManager("database.yml");
        databaseFile.addDefault("Database.Enabled", false);
        databaseFile.addDefault("Database.Type", "SQLITE");
        databaseFile.addDefault("Database.SQLite.Path", "plugins/LuckPrefix/storage");
        databaseFile.addDefault("Database.SQLite.FileName", "sqlite.db");
        databaseFile.addDefault("Database.MariaDB.Host", "localhost");
        databaseFile.addDefault("Database.MariaDB.Port", 3306);
        databaseFile.addDefault("Database.MariaDB.Database", "luckprefix");
        databaseFile.addDefault("Database.MariaDB.User", "luckprefix");
        databaseFile.addDefault("Database.MariaDB.Password", "");
        databaseFile.saveDefaults();
        databaseFile.setComments("Database", new ArrayList<>(List.of("The both supported types are sqlite and mariadb, otherwise you can still disable it for file-save")));
        databaseFile.saveComments();
        groupsFile = new FileManager("groups.yml");
        groupsFile.addDefault("Groups.default.Prefix", "<gray>Player");
        groupsFile.addDefault("Groups.default.Suffix", "");
        groupsFile.addDefault("Tabformat", "<prefix> <dark_gray>- <gray><player>");
        groupsFile.addDefault("Chatformat", "<prefix <dark_gray>- <gray><player> <dark_gray>» <gray><message>");
        groupsFile.addDefault("SortID", 999);
        groupsFile.addDefault("NameColor", "gray");
        groupsFile.saveDefaults();
        groupsFile.setComments("Groups", new ArrayList<>(List.of("The groups")));
        groupsFile.setComments("Groups.default", new ArrayList<>(List.of("The name of the group")));
        groupsFile.setComments("Groups.default.Prefix", new ArrayList<>(List.of(
                "Prefix, suffix, tabformat and chatformat are working with the adventure minimessage format,",
                "but the legacy '&'-codes also work",
                "https://docs.advntr.dev/minimessage/format.html"
        )));
        groupsFile.setComments("SortID", new ArrayList<>(List.of(
                "The sort-id is important for the ordner on the tablist.",
                "The lowest value is on top and the highest at the bottom.",
                "You only can set the sort-id between 1 and 999. More can execute issues."
        )));
        groupsFile.setComments("NameColor", new ArrayList<>(List.of("The color of the name above a player")));
        groupsFile.saveComments();
        if (databaseFile.getValue("Database.Enabled").asBoolean()) {
            connection = DatabaseManager.createDatabaseConnection();
        }
        adventure = BukkitAudiences.create(instance);
        luckPerms = LuckPermsProvider.get();
        registry = new Registry(instance);
        registry.registerCommand("luckprefix", new LuckPrefixCommand());
        registry.registerListener(new JoinListener());
        registry.registerListener(new QuitListener());
        registry.registerListener(new ChatListener());
        if (connection != null) {
            databaseManager = new DatabaseManager(connection);
        }
        playerManager = new PlayerManager();
        groupManager = new GroupManager();
        groupListener = new GroupListener();
        groupListener.createGroup();
        groupListener.deleteGroup();
        luckPerms.getGroupManager().getLoadedGroups().forEach(group -> {
            if (groupsFile.contains("Groups." + group.getName())) {
                groupManager.createGroup(group.getName());
                return;
            }
            getLogger().warning("Group '" + group.getName() + "' can't be loaded. Please check your config!");
        });
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerManager.setPlayerListName(player.getUniqueId());
            }
        }, 1, getConfig().getLong("UpdateTime") * 20);
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
