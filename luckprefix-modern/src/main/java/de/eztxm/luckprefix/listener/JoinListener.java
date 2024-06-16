package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
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
        Group group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
        FileConfiguration config = LuckPrefix.getInstance().getConfig();
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(LuckPrefix.getInstance(), () -> {
            LuckPrefix.getInstance().getGroups().setGroup(group, player);
        }, 1, config.getLong("UpdateTime"));
        LuckPrefix.getInstance().getPlayerUtil().addJoinScheduler(player.getUniqueId(), bukkitTask);
    }
}
