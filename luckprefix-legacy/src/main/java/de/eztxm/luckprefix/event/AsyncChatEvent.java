package de.eztxm.luckprefix.event;

import de.eztxm.luckprefix.LuckPrefix;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncChatEvent implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        LuckPerms luckPermsApi = LuckPermsProvider.get();
        User user = luckPermsApi.getUserManager().getUser(event.getPlayer().getUniqueId());
        if (event.getPlayer().hasPermission(LuckPrefix.getInstance().getConfig().getString("ColoredPermission"))) {
            event.setFormat(LuckPrefix.getInstance().getConfig().getString("Groups." + user.getPrimaryGroup() + ".ChatFormat")
                    .replace('&', '§')
                    .replace("%>>", "»")
                    .replace("%prefix", LuckPrefix.getInstance().getConfig().getString("Groups." + user.getPrimaryGroup() + ".Prefix").replace('&', '§'))
                    .replace("%player", event.getPlayer().getName())
                    .replace("%message", event.getMessage().replace('&', '§')));
        } else {
            event.setFormat(LuckPrefix.getInstance().getConfig().getString("Groups." + user.getPrimaryGroup() + ".ChatFormat")
                    .replace('&', '§')
                    .replace("%>>", "»")
                    .replace("%prefix", LuckPrefix.getInstance().getConfig().getString("Groups." + user.getPrimaryGroup() + ".Prefix").replace('&', '§'))
                    .replace("%player", event.getPlayer().getName())
                    .replace("%message", event.getMessage()));
        }
    }
}
