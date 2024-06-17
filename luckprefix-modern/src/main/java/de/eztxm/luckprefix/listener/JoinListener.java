package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.GroupManager;
import de.eztxm.luckprefix.util.PlayerManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        PlayerManager playerManager = LuckPrefix.getInstance().getPlayerManager();
        GroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
        String group = user.getPrimaryGroup();
        FileConfiguration config = LuckPrefix.getInstance().getConfig();
        playerManager.initializePlayer(player.getUniqueId(), group);
        groupManager.setupGroups(player);
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(LuckPrefix.getInstance(), () -> {
            groupManager.setGroups(player);
        }, 1, config.getLong("UpdateTime") * 20);
        playerManager.addJoinScheduler(player.getUniqueId(), bukkitTask);
        playerManager.addUserGroup(player.getUniqueId(), group);
    }
}
