package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.api.manager.IPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        IPlayerManager playerManager = LuckPrefix.getInstance().getPlayerManager();
        if (playerManager.getUserGroups().containsKey(player.getUniqueId())) {
            playerManager.removeUserGroup(player.getUniqueId());
        }
    }
}
