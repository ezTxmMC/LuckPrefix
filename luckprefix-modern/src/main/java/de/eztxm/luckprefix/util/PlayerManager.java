package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.group.GroupManager;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerManager {
    private final Map<UUID, String> userGroups;

    public PlayerManager() {
        this.userGroups = new HashMap<>();
    }

    public void initializePlayer(UUID uuid, String group) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        userGroups.put(uuid, group);
    }

    public void setPlayerListName(UUID uuid, String luckPermsGroup) {
        GroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        if (!userGroups.containsKey(uuid)) return;
        String group = userGroups.get(uuid);
        if (group.equalsIgnoreCase(luckPermsGroup)) return;
        group = luckPermsGroup;
        userGroups.put(uuid, group);
        if (groupManager.getPrefixByGroup(group) == null) {
            if (groupManager.getSuffixByGroup(group) == null) {
                return;
            }
            TagResolver.Single suffix = Placeholder.component("suffix", new Text(groupManager.getSuffixByGroup(group)).placeholders(player).miniMessage());
            player.setPlayerListName(new Text(groupManager.getTabFormatByGroup(group)).placeholders(player).legacyMiniMessage(
                    Placeholder.component("prefix", Component.text("")), suffix, Placeholder.component("player", new Text(player.getName()).placeholders(player).component())));
            return;
        }
        TagResolver.Single prefix = Placeholder.component("prefix", new Text(groupManager.getPrefixByGroup(group)).placeholders(player).miniMessage());
        if (groupManager.getSuffixByGroup(group) == null) {
            player.setPlayerListName(new Text(groupManager.getTabFormatByGroup(group)).placeholders(player).legacyMiniMessage(
                    prefix, Placeholder.component("suffix", Component.text("")), Placeholder.component("player", new Text(player.getName()).placeholders(player).component())));
            return;
        }
        TagResolver.Single suffix = Placeholder.component("suffix", new Text(groupManager.getSuffixByGroup(group)).placeholders(player).miniMessage());
        player.setPlayerListName(new Text(groupManager.getTabFormatByGroup(group)).placeholders(player).legacyMiniMessage(
                prefix, suffix, Placeholder.component("player", new Text(player.getName()).placeholders(player).component())));
    }

    public void setUserGroup(UUID uuid, String group) {
        this.userGroups.put(uuid, group);
    }

    public void removeUserGroup(UUID uuid) {
        this.userGroups.remove(uuid);
    }
}
