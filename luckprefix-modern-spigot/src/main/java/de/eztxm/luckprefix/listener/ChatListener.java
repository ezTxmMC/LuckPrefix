package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.GroupManager;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        FileConfiguration config = LuckPrefix.getInstance().getConfig();
        FileConfiguration groupsConfig = LuckPrefix.getInstance().getGroupsFile().getConfiguration();
        GroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
        String group = user.getPrimaryGroup();
        if (player.hasPermission(config.getString("ColoredPermission"))) {
            event.setFormat(new Text(groupsConfig.getString(group + ".Chatformat")).legacyMiniMessage(
                    Placeholder.component("prefix", new Text(groupManager.getGroupPrefix().get(group)).miniMessage()),
                    Placeholder.component("suffix", new Text(groupManager.getGroupSuffix().get(group)).miniMessage()),
                    Placeholder.component("player", Component.text(player.getName())),
                    Placeholder.component("message", Component.text(ChatColor.translateAlternateColorCodes('&', event.getMessage())))));
            return;
        }
        event.setFormat(new Text(groupsConfig.getString(group + ".Chatformat")).legacyMiniMessage(
                Placeholder.component("prefix", new Text(groupManager.getGroupPrefix().get(group)).miniMessage()),
                Placeholder.component("suffix", new Text(groupManager.getGroupSuffix().get(group)).miniMessage()),
                Placeholder.component("player", Component.text(player.getName())),
                Placeholder.component("message", Component.text(event.getMessage()))));
    }
}
