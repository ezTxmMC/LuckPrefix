package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.scoreboard.Teams;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(scoreboard);
        LuckPerms luckPermsApi = LuckPermsProvider.get();
        luckPermsApi.getGroupManager().getLoadedGroups().forEach(group -> {
            if (LuckPrefix.getInstance().getConfig().contains("Groups." + group.getName())) {
                Teams.createTeam(scoreboard, group);
            } else {
                LuckPrefix.getInstance().getLogger().warning("§cGroup '" + group.getName() + "' can't be loaded. Please check your config!");
            }
        });
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(LuckPrefix.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(Teams::setTeam), 5, 20);
        LuckPrefix.JOIN_SCHEDULERS.put(player, bukkitTask);
    }
}
