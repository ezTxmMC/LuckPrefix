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
        event.renderer(new ChatRenderer() {
            @Override
            public @NotNull Component render(@NotNull Player player, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
                if (player.hasPermission(config.getString("ColoredPermission"))) {
                    return new Text(groupsConfig.getString(group + ".Chatformat")).miniMessage(
                            Placeholder.component("prefix", new Text(groupManager.getGroupPrefix().get(group)).miniMessage()),
                            Placeholder.component("suffix", new Text(groupManager.getGroupSuffix().get(group)).miniMessage()),
                            Placeholder.component("player", Component.text(player.getName())),
                            Placeholder.component("message", Text.parseLegacy(event.message())));
                }
                return new Text(groupsConfig.getString(group + ".Chatformat")).miniMessage(
                        Placeholder.component("prefix", new Text(groupManager.getGroupPrefix().get(group)).miniMessage()),
                        Placeholder.component("suffix", new Text(groupManager.getGroupSuffix().get(group)).miniMessage()),
                        Placeholder.component("player", Component.text(player.getName())),
                        Placeholder.component("message", event.message()));
            }
        });
    }
}
