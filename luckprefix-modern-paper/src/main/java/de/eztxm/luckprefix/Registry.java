package de.eztxm.luckprefix;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public record Registry(Plugin plugin) {

    public void registerCommand(String name, CommandExecutor executor) {
        this.plugin.getServer().getPluginCommand(name).setExecutor(executor);
        LuckPrefix.getInstance().getDebugLog().info("Command " + name + " registered with Class " + executor.getClass());
    }

    public void registerListener(Listener listener) {
        this.plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        LuckPrefix.getInstance().getDebugLog().info("Listener " + listener.getClass() + " registered");
    }
}
