package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerUtil playerUtil = LuckPrefix.getInstance().getPlayerUtil();
        if (playerUtil.getJoinSchedulers().containsKey(player.getUniqueId())) {
            playerUtil.cancelJoinScheduler(player.getUniqueId());
            playerUtil.removeJoinScheduler(player.getUniqueId());
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
}
