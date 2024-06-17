package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerManager playerManager = LuckPrefix.getInstance().getPlayerManager();
        if (playerManager.getJoinSchedulers().containsKey(player.getUniqueId())) {
            playerManager.cancelJoinScheduler(player.getUniqueId());
            playerManager.removeJoinScheduler(player.getUniqueId());
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
}
