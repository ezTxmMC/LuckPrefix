package de.eztxm.luckprefix.util;

import org.bukkit.plugin.Plugin;

public record DependUtil(Plugin plugin) {

    public boolean isLuckPermsEnabled() {
        return this.plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms");
    }

    public boolean isPlaceholderAPIEnabled() {
        return this.plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }
}
