package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class GroupManager {
    private final List<String> groups;
    private final Map<String, String> groupPrefix;
    private final Map<String, String> groupSuffix;
    private final Map<String, String> groupTabformat;
    private final Map<String, String> groupChatformat;
    private final Map<String, String> groupID;
    private final Map<String, ChatColor> groupColor;

    public GroupManager() {
        this.groups = new ArrayList<>();
        this.groupPrefix = new HashMap<>();
        this.groupSuffix = new HashMap<>();
        this.groupTabformat = new HashMap<>();
        this.groupChatformat = new HashMap<>();
        this.groupID = new HashMap<>();
        this.groupColor = new HashMap<>();
    }

    public void setGroups(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            PlayerManager playerManager = LuckPrefix.getInstance().getPlayerManager();
            Team team = scoreboard.getTeam(this.groupID.get(playerManager.getUserGroups().get(onlinePlayer.getUniqueId()))
                    + playerManager.getUserGroups().get(onlinePlayer.getUniqueId()));
            if (team == null) {
                scoreboard.getTeam(this.groupID.get("default") + "default").addEntry(player.getName());
                return;
            }
            Team currentTeam = scoreboard.getEntryTeam(onlinePlayer.getName());
            if (currentTeam == null) {
                team.addEntry(onlinePlayer.getName());
                return;
            }
            if (team.getEntries().contains(player.getName())) {
                return;
            }
            team.addEntry(onlinePlayer.getName());
        }
    }

    public void createGroup(String group) {
        FileConfiguration config = LuckPrefix.getInstance().getGroupsFile().getConfiguration();
        this.groups.add(group);
        if (config.get(group) == null) {
            this.groupPrefix.put(group, config.getString("default.Prefix"));
            this.groupSuffix.put(group, config.getString("default.Suffix"));
            this.groupTabformat.put(group, config.getString("default.Tabformat"));
            this.groupChatformat.put(group, config.getString("default.Chatformat"));
            String sortIDraw = String.valueOf(config.getInt("default.SortID"));
            int maxLength = 4;
            int currentLength = sortIDraw.length();
            String sortIDBuilt = "0".repeat(Math.max(0, maxLength - currentLength)) + sortIDraw;
            this.groupID.put(group, sortIDBuilt);
            this.groupColor.put(group, ChatColor.valueOf(config.getString("default.NameColor").toUpperCase()));
            return;
        }
        this.groupPrefix.put(group, config.getString(group + ".Prefix"));
        this.groupSuffix.put(group, config.getString(group + ".Suffix"));
        this.groupTabformat.put(group, config.getString(group + ".Tabformat"));
        this.groupChatformat.put(group, config.getString(group + ".Chatformat"));
        String sortIDraw = String.valueOf(config.getInt(group + ".SortID"));
        int maxLength = 4;
        int currentLength = sortIDraw.length();
        String sortIDBuilt = "0".repeat(Math.max(0, maxLength - currentLength)) + sortIDraw;
        this.groupID.put(group, sortIDBuilt);
        this.groupColor.put(group, ChatColor.valueOf(config.getString(group + ".NameColor").toUpperCase()));
    }

    public void setupGroups(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        for (String group : this.groups) {
            Team team = scoreboard.registerNewTeam(this.groupID.get(group) + group);
            if (this.groupPrefix.get(group) != null) {
                team.setPrefix(ChatColor.translateAlternateColorCodes('&',
                        LegacyComponentSerializer.legacyAmpersand().serialize(MiniMessage.miniMessage().deserialize(this.groupTabformat.get(group)
                                .replace("<prefix>", this.groupPrefix.get(group))
                                .replace("<player>", "")
                                .replace("<suffix>", "")))));
            }
            if (this.groupSuffix.get(group) != null) {
                team.setSuffix(" " + ChatColor.translateAlternateColorCodes('&',
                        LegacyComponentSerializer.legacyAmpersand().serialize(MiniMessage.miniMessage().deserialize(this.groupSuffix.get(group)))));
            }
            if (this.groupColor.get(group) != null) {
                team.setColor(this.groupColor.get(group));
            }
        }
    }

    public void deleteGroup(String group) {
        this.groups.remove(group);
        this.groupPrefix.remove(group);
        this.groupSuffix.remove(group);
        this.groupTabformat.remove(group);
        this.groupChatformat.remove(group);
        this.groupID.remove(group);
        this.groupColor.remove(group);
    }
}
