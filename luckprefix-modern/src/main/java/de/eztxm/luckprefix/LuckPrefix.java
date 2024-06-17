package de.eztxm.luckprefix;

import de.eztxm.luckprefix.command.LuckPrefixCommand;
import de.eztxm.luckprefix.listener.ChatListener;
import de.eztxm.luckprefix.listener.GroupListener;
import de.eztxm.luckprefix.listener.JoinListener;
import de.eztxm.luckprefix.listener.QuitListener;
import de.eztxm.luckprefix.util.GroupManager;
import de.eztxm.luckprefix.util.PlayerManager;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class LuckPrefix extends JavaPlugin {
    @Getter
    private static LuckPrefix instance;

    private String prefix;
    private BukkitAudiences adventure;
    private LuckPerms luckPerms;
    private Registry registry;
    private PlayerManager playerManager;
    private GroupManager groupManager;
    private GroupListener groupListener;

    @Override
    public void onEnable() {
        instance = this;
        prefix = "<#77ef77>LuckPrefix <dark_gray>| <gray>";
        adventure = BukkitAudiences.create(instance);
        luckPerms = LuckPermsProvider.get();
        registry = new Registry(instance);
        registry.registerCommand("luckprefix", new LuckPrefixCommand());
        registry.registerListener(new JoinListener());
        registry.registerListener(new QuitListener());
        registry.registerListener(new ChatListener());
        playerManager = new PlayerManager();
        groupManager = new GroupManager();
        groupListener = new GroupListener();
        groupListener.createGroup();
        groupListener.deleteGroup();
        saveDefaultConfig();
        luckPerms.getGroupManager().getLoadedGroups().forEach(group -> {
            if (getConfig().contains("Groups." + group.getName())) {
                groupManager.createGroup(group.getName());
                return;
            }
            getLogger().warning("Group '" + group.getName() + "' can't be loaded. Please check your config!");
        });
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) return;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!playerManager.getUserGroups().containsKey(onlinePlayer.getUniqueId())) return;
                String group = playerManager.getUserGroups().get(onlinePlayer.getUniqueId());
                onlinePlayer.setPlayerListName(ChatColor.translateAlternateColorCodes('&', LegacyComponentSerializer.legacyAmpersand().serialize(MiniMessage.miniMessage().deserialize(groupManager.getGroupTabformat().get(group),
                        Placeholder.component("prefix", MiniMessage.miniMessage().deserialize(groupManager.getGroupPrefix().get(group))),
                        Placeholder.component("suffix", MiniMessage.miniMessage().deserialize(groupManager.getGroupSuffix().get(group))),
                        Placeholder.component("player", Component.text(onlinePlayer.getName()))))));
            }
        }, 1, getConfig().getLong("UpdateTime"));
    }

    @Override
    public void onDisable() {
        instance = null;
        registry = null;
        playerManager = null;
        groupManager = null;
        groupListener = null;
    }
}
