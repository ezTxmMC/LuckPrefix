package de.eztxm.luckprefix;

import de.eztxm.luckprefix.command.LuckPrefixCommand;
import de.eztxm.luckprefix.listener.ChatListener;
import de.eztxm.luckprefix.listener.GroupListener;
import de.eztxm.luckprefix.listener.JoinListener;
import de.eztxm.luckprefix.listener.QuitListener;
import de.eztxm.luckprefix.util.Groups;
import de.eztxm.luckprefix.util.PlayerUtil;
import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class LuckPrefix extends JavaPlugin {
    @Getter
    private static LuckPrefix instance;

    private LuckPerms luckPerms;
    private Registry registry;
    private PlayerUtil playerUtil;
    private Groups groups;
    private GroupListener groupListener;

    @Override
    public void onEnable() {
        instance = this;
        luckPerms = LuckPermsProvider.get();
        registry = new Registry(instance);
        registry.registerCommand("luckprefix", new LuckPrefixCommand());
        registry.registerListener(new JoinListener());
        registry.registerListener(new QuitListener());
        registry.registerListener(new ChatListener());
        playerUtil = new PlayerUtil();
        groups = new Groups();
        groupListener = new GroupListener();
        groupListener.createGroup();
        groupListener.deleteGroup();
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        instance = null;
        registry = null;
        playerUtil = null;
    }
}
