package de.eztxm.luckprefix.depend;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.manager.GroupManager;
import io.papermc.paper.plugin.configuration.PluginMeta;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.luckperms.api.LuckPerms;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class LuckPrefixPlaceholderExtension extends PlaceholderExpansion {
    private final PluginMeta pluginMeta;

    public LuckPrefixPlaceholderExtension(PluginMeta pluginMeta) {
        this.pluginMeta = pluginMeta;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "luckprefix";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", pluginMeta.getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return pluginMeta.getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        LuckPerms luckPerms = LuckPrefix.getInstance().getLuckPerms();
        GroupManager groupManager = (GroupManager) LuckPrefix.getInstance().getGroupManager();
        String playerGroup = luckPerms.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup().toLowerCase();
        switch (params.toLowerCase()) {
            case "prefix" -> {
                return groupManager.getPrefixByGroup(playerGroup);
            }
            case "suffix" -> {
                return groupManager.getSuffixByGroup(playerGroup);
            }
            case "tabformat" -> {
                return groupManager.getTabFormatByGroup(playerGroup);
            }
            case "chatformat" -> {
                return groupManager.getChatFormatByGroup(playerGroup);
            }
            case "sortid" -> {
                return groupManager.getSortIdAsStringByGroup(playerGroup);
            }
            case "namecolor" -> {
                return groupManager.getNameColorByGroup(playerGroup).asHexString();
            }
        }
        return null;
    }
}
