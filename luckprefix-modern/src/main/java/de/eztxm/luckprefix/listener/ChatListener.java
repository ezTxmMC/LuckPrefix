package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(event.getPlayer().getUniqueId());
        FileConfiguration config = LuckPrefix.getInstance().getConfig();
        if (event.getPlayer().hasPermission(LuckPrefix.getInstance().getConfig().getString("ColoredPermission"))) {
            event.setFormat(LuckPrefix.getInstance().getConfig().getString("Groups." + user.getPrimaryGroup() + ".ChatFormat")
                    .replace('&', '§')
                    .replace("%>>", "»")
                    .replace("%prefix", ChatColor.translateAlternateColorCodes('&',
                            LuckPrefix.getInstance().getConfig().getString("Groups." + user.getPrimaryGroup() + ".Prefix")))
                    .replace("%player", event.getPlayer().getName())
                    .replace("%message", ChatColor.translateAlternateColorCodes((char) config.get("ColorCodeChar"), event.getMessage().replace("%", "%%"))));
            return;
        }
        event.setFormat(LuckPrefix.getInstance().getConfig().getString("Groups." + user.getPrimaryGroup() + ".ChatFormat")
                .replace('&', '§')
                .replace("%>>", "»")
                .replace("%prefix", ChatColor.translateAlternateColorCodes('&',
                        LuckPrefix.getInstance().getConfig().getString("Groups." + user.getPrimaryGroup() + ".Prefix")))
                .replace("%player", event.getPlayer().getName())
                .replace("%message", event.getMessage().replace("%", "%%")));
    }
}
