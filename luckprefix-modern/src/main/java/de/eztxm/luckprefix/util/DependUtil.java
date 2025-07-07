package de.eztxm.luckprefix.util;

import org.bukkit.plugin.Plugin;

public class DependUtil {
    private final Plugin plugin;

    public DependUtil(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isLuckPermsEnabled() {
        return this.plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms");
    }

    public boolean isPlaceholderAPIEnabled() {
        return this.plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }
}
