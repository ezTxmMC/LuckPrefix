package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (LuckPrefix.JOIN_SCHEDULERS.containsKey(player)) {
            LuckPrefix.JOIN_SCHEDULERS.get(player).cancel();
            LuckPrefix.JOIN_SCHEDULERS.remove(player);
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
}
