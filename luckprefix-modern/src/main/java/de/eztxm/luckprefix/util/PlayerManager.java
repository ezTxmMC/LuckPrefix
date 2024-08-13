package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerManager {
    private final Map<UUID, BukkitTask> joinSchedulers;
    private final Map<UUID, String> userGroups;

    public PlayerManager() {
        this.joinSchedulers = new HashMap<>();
        this.userGroups = new HashMap<>();
    }

    public void initializePlayer(UUID uuid, String group) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        userGroups.put(uuid, group);
    }

    public void setPlayerListName(UUID uuid) {
        GroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        if (!userGroups.containsKey(uuid)) return;
        String group = userGroups.get(uuid);
        if (groupManager.getGroupPrefix().get(group) == null) {
            if (groupManager.getGroupSuffix().get(group) == null) {
                return;
            }
            TagResolver.Single suffix = Placeholder.component("suffix", new Text(groupManager.getGroupSuffix().get(group)).miniMessage());
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&',
                    LegacyComponentSerializer.legacyAmpersand().serialize(MiniMessage.miniMessage().deserialize(groupManager.getGroupTabformat().get(group),
                            Placeholder.component("prefix", Component.text("")),
                            suffix,
                            Placeholder.component("player", Component.text(player.getName()))))));
            return;
        }
        TagResolver.Single prefix = Placeholder.component("prefix", new Text(groupManager.getGroupPrefix().get(group)).miniMessage());
        if (groupManager.getGroupSuffix().get(group) == null) {
            player.setPlayerListName(ChatColor.translateAlternateColorCodes('&',
                    LegacyComponentSerializer.legacyAmpersand().serialize(MiniMessage.miniMessage().deserialize(groupManager.getGroupTabformat().get(group),
                            prefix,
                            Placeholder.component("suffix", Component.text("")),
                            Placeholder.component("player", Component.text(player.getName()))))));
            return;
        }
        TagResolver.Single suffix = Placeholder.component("suffix", new Text(groupManager.getGroupSuffix().get(group)).miniMessage());
        player.setPlayerListName(ChatColor.translateAlternateColorCodes('&',
                LegacyComponentSerializer.legacyAmpersand().serialize(MiniMessage.miniMessage().deserialize(groupManager.getGroupTabformat().get(group),
                        prefix,
                        suffix,
                        Placeholder.component("player", Component.text(player.getName()))))));
    }

    public void addJoinScheduler(UUID uuid, BukkitTask bukkitTask) {
        this.joinSchedulers.put(uuid, bukkitTask);
    }

    public void addUserGroup(UUID uuid, String group) {
        this.userGroups.put(uuid, group);
    }

    public void cancelJoinScheduler(UUID uuid) {
        this.joinSchedulers.get(uuid).cancel();
    }

    public void removeJoinScheduler(UUID uuid) {
        this.joinSchedulers.remove(uuid);
    }

    public void removeUserGroup(UUID uuid) {
        this.userGroups.remove(uuid);
    }
}
