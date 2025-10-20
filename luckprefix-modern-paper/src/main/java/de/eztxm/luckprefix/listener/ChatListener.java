package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.GroupsConfig;
import de.eztxm.luckprefix.common.config.MainConfig;
import de.eztxm.luckprefix.util.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        ConfigService service = LuckPrefix.getInstance().getConfigService();
        MainConfig config = service.of(MainConfig.class);
        GroupsConfig groupsConfig = service.of(GroupsConfig.class);
        assert user != null;
        String group = user.getPrimaryGroup();
        String effectiveGroup = groupsConfig.hasGroup(group) ? group : "default";
        if (!effectiveGroup.equals(group)) {
            LuckPrefix.getInstance().getLogger().warning(
                    "Gruppe '" + group + "' nicht in groups.yml gefunden – benutze 'default'.");
        }
        String format = groupsConfig.getChatFormat(group);
        if (format == null || format.isBlank()) {
            format = "<prefix> <dark_gray>- <gray><player> <dark_gray>» <gray><message>";
            LuckPrefix.getInstance().getLogger().warning(
                    "Chatformat für '" + effectiveGroup + "' fehlt/leer – nutze Fallback.");
        }
        String prefStr = groupsConfig.getPrefix(group);
        String suffStr = groupsConfig.getSuffix(group);
        final String formatFinal = format;
        final String prefixFinal = prefStr;
        final String suffixFinal = suffStr;
        event.renderer((audience, displayName, message, viewer) -> new Text(formatFinal).placeholders(audience).miniMessage(
                Placeholder.component("prefix", new Text(prefixFinal).placeholders(audience).miniMessage()),
                Placeholder.component("suffix", new Text(suffixFinal).placeholders(audience).miniMessage()),
                Placeholder.component("player", Component.text(audience.getName())),
                Placeholder.component("message", player.hasPermission(config.getColoredPermission()) ? Text.parseLegacy(message) : message)
        ));
    }
}
