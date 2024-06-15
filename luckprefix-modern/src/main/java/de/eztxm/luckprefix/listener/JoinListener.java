package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LuckPerms luckPermsApi = LuckPermsProvider.get();
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        luckPermsApi.getGroupManager().getLoadedGroups().forEach(group -> {
            if (LuckPrefix.getInstance().getConfig().contains("Groups." + group.getName())) {
                LuckPrefix.getInstance().getGroups().createGroup(group);
                return;
            }
            LuckPrefix.getInstance().getLogger().warning("§cGroup '" + group.getName() + "' can't be loaded. Please check your config!");
        });
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(LuckPrefix.getInstance(),
                () -> Bukkit.getOnlinePlayers().forEach(players -> LuckPrefix.getInstance().getGroups().setGroup(players)), 5, 20);
        LuckPrefix.getInstance().getPlayerUtil().addJoinScheduler(player.getUniqueId(), bukkitTask);
    }
}
