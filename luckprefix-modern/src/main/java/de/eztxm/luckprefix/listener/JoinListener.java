package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.api.manager.IGroupManager;
import de.eztxm.luckprefix.api.manager.IPlayerManager;
import de.eztxm.luckprefix.common.util.UpdateChecker;
import de.eztxm.luckprefix.util.LuckPlayer;
import de.eztxm.luckprefix.util.Text;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return;
        }
        IPlayerManager playerManager = LuckPrefix.getInstance().getPlayerManager();
        IGroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
        String group = user.getPrimaryGroup();
        playerManager.initializePlayer(player.getUniqueId(), group);
        groupManager.setupGroups(new LuckPlayer(player));
        playerManager.setUserGroup(player.getUniqueId(), group);
        UpdateChecker checker = LuckPrefix.getInstance().getUpdateChecker();
        if (player.hasPermission("luckprefix.update") && !checker.isLatestVersion(LuckPrefix.isDevelopment())) {
            player.sendMessage(new Text("There is a new update available: <u><click:open_url:https://modrinth.com/plugin/luckprefix>" + checker.getCachedLatestVersion() + "</click></u>").prefixMiniMessage());
        }
    }
}
