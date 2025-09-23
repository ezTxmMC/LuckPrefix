package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.GroupManager;
import de.eztxm.luckprefix.util.Text;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        FileConfiguration config = LuckPrefix.getInstance().getConfig();
        FileConfiguration groupsConfig = LuckPrefix.getInstance().getGroupsFile().getConfiguration();
        GroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
        String group = user.getPrimaryGroup();

        String effectiveGroup = groupsConfig.isConfigurationSection(group) ? group : "default";
        if (!effectiveGroup.equals(group)) {
            LuckPrefix.getInstance().getLogger().warning(
                    "Gruppe '" + group + "' nicht in groups.yml gefunden – benutze 'default'.");
        }

        String format = groupsConfig.getString(effectiveGroup + ".Chatformat");
        if (format == null || format.isBlank()) {
            format = "<prefix> <dark_gray>- <gray><player> <dark_gray>» <gray><message>";
            LuckPrefix.getInstance().getLogger().warning(
                    "Chatformat für '" + effectiveGroup + "' fehlt/leer – nutze Fallback.");
        }

        String prefStr = groupsConfig.getString(effectiveGroup + ".Prefix");
        if (prefStr == null) prefStr = "";

        String suffStr = groupsConfig.getString(effectiveGroup + ".Suffix");
        if (suffStr == null) suffStr = "";

        final String formatFinal = format;
        final String prefixFinal = prefStr;
        final String suffixFinal = suffStr;

        if(player.hasPermission(config.getString("ColoredPermission"))) {
            event.renderer((audience, displayName, message, viewer) -> new Text(formatFinal).miniMessage(
                    Placeholder.component("prefix", new Text(prefixFinal).placeholders(audience).miniMessage()),
                    Placeholder.component("suffix", new Text(suffixFinal).placeholders(audience).miniMessage()),
                    Placeholder.component("player", Component.text(audience.getName())),
                    Placeholder.component("message", Text.parseLegacy(message))
            ));
            return;
        }
        event.renderer((audience, displayName, message, viewer) -> new Text(formatFinal).miniMessage(
                Placeholder.component("prefix", new Text(prefixFinal).placeholders(audience).miniMessage()),
                Placeholder.component("suffix", new Text(suffixFinal).placeholders(audience).miniMessage()),
                Placeholder.component("player", Component.text(audience.getName())),
                Placeholder.component("message", message)
        ));
    }

}
