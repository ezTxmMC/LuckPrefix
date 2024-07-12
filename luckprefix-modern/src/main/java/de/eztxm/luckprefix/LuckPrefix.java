package de.eztxm.luckprefix;

import de.eztxm.api.database.SQLConnection;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class LuckPrefix extends JavaPlugin {
    @Getter
    private static LuckPrefix instance;

    private String prefix;
    private ConfigManager databaseFile;
    private ConfigManager groupsFile;
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
        databaseFile = ConfigUtil.addDatabaseDefault("database.yml");
        groupsFile = ConfigUtil.addGroupsDefault("groups.yml");
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
            if (groupsFile.contains(group.getName())) {
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
