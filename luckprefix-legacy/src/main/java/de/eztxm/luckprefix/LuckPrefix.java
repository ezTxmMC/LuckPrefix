package de.eztxm.luckprefix;

import de.eztxm.luckprefix.event.AsyncChatEvent;
import de.eztxm.luckprefix.event.JoinEvent;
import de.eztxm.luckprefix.event.QuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public final class LuckPrefix extends JavaPlugin {
    private static LuckPrefix instance;
    public static final HashMap<Player, BukkitTask> JOIN_SCHEDULERS = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        registerListeners();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new JoinEvent(), this);
        pluginManager.registerEvents(new QuitEvent(), this);
        pluginManager.registerEvents(new AsyncChatEvent(), this);
    }

    public static LuckPrefix getInstance() {
        return instance;
    }
}
