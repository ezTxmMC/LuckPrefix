package de.eztxm.luckprefix;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class Registry {
    private final Plugin plugin;

    public Registry(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerCommand(String name, CommandExecutor executor) {
        this.plugin.getServer().getPluginCommand(name).setExecutor(executor);
    }

    public void registerListener(Listener listener) {
        this.plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}
