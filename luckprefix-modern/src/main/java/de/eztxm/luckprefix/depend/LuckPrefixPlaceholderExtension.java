package de.eztxm.luckprefix.depend;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.GroupManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.luckperms.api.LuckPerms;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuckPrefixPlaceholderExtension extends PlaceholderExpansion {
    private final Plugin plugin;

    public LuckPrefixPlaceholderExtension(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "luckprefix";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        LuckPerms luckPerms = LuckPrefix.getInstance().getLuckPerms();
        GroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
        String playerGroup = luckPerms.getUserManager().getUser(player.getUniqueId()).getPrimaryGroup().toLowerCase();
        switch (params.toLowerCase()) {
            case "prefix" -> {
                return groupManager.getPrefixByGroup().get(playerGroup);
            }
            case "suffix" -> {
                return groupManager.getSuffixByGroup().get(playerGroup);
            }
            case "tabformat" -> {
                return groupManager.getTabFormatByGroup().get(playerGroup);
            }
            case "chatformat" -> {
                return groupManager.getChatFormatByGroup().get(playerGroup);
            }
            case "sortid" -> {
                return groupManager.getSortIdByGroup().get(playerGroup);
            }
            case "namecolor" -> {
                return groupManager.getNameColorByGroup().get(playerGroup).toString();
            }
        }
        return null;
    }
}
