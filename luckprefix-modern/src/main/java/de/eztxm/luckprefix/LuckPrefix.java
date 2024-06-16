package de.eztxm.luckprefix;

import de.eztxm.luckprefix.command.LuckPrefixCommand;
import de.eztxm.luckprefix.listener.ChatListener;
import de.eztxm.luckprefix.listener.GroupListener;
import de.eztxm.luckprefix.listener.JoinListener;
import de.eztxm.luckprefix.listener.QuitListener;
import de.eztxm.luckprefix.util.Groups;
import de.eztxm.luckprefix.util.PlayerUtil;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class LuckPrefix extends JavaPlugin {
    @Getter
    private static LuckPrefix instance;

    private String prefix;
    private BukkitAudiences adventure;
    private LuckPerms luckPerms;
    private Registry registry;
    private PlayerUtil playerUtil;
    private Groups groups;
    private GroupListener groupListener;

    @Override
    public void onEnable() {
        instance = this;
        prefix = "<#77ef77>LuckPrefix <dark_gray>| <gray>";
        adventure = BukkitAudiences.create(instance);
        luckPerms = LuckPermsProvider.get();
        luckPerms.getGroupManager().getLoadedGroups().forEach(group -> {
            if (LuckPrefix.getInstance().getConfig().contains("Groups." + group.getName())) {
                LuckPrefix.getInstance().getGroups().createGroup(group);
                return;
            }
            LuckPrefix.getInstance().getLogger().warning("§cGroup '" + group.getName() + "' can't be loaded. Please check your config!");
        });
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
        groups = null;
        groupListener = null;
    }
}
