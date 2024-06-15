package de.eztxm.luckprefix;

import de.eztxm.luckprefix.listener.ChatListener;
import de.eztxm.luckprefix.listener.JoinListener;
import de.eztxm.luckprefix.listener.QuitListener;
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
        pluginManager.registerEvents(new JoinListener(), this);
        pluginManager.registerEvents(new QuitListener(), this);
        pluginManager.registerEvents(new ChatListener(), this);
    }

    public static LuckPrefix getInstance() {
        return instance;
    }
}
