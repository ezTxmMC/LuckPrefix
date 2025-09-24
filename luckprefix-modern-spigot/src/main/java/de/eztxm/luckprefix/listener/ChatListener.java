package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.GroupsConfig;
import de.eztxm.luckprefix.common.config.MainConfig;
import de.eztxm.luckprefix.util.GroupManager;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.ChatColor;
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
        ConfigService service = LuckPrefix.getInstance().getConfigService();
        MainConfig config = service.of(MainConfig.class);
        GroupsConfig groupsConfig = service.of(GroupsConfig.class);
        GroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
        String group = user.getPrimaryGroup();
        event.setFormat(new Text(groupsConfig.getChatFormat(group)).placeholders(player).legacyMiniMessage(
                Placeholder.component("prefix", new Text(groupManager.getPrefixByGroup().get(group)).placeholders(player).miniMessage()),
                Placeholder.component("suffix", new Text(groupManager.getSuffixByGroup().get(group)).placeholders(player).miniMessage()),
                Placeholder.component("player", new Text(player.getName()).placeholders(player).component()),
                Placeholder.component("message",
                        player.hasPermission(config.getColoredPermission()) ?
                                Component.text(ChatColor.translateAlternateColorCodes('&', event.getMessage())) :
                                Component.text(event.getMessage())
                )));
    }
}
