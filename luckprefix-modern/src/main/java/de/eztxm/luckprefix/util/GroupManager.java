package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import lombok.Getter;
import net.luckperms.api.model.group.Group;
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

    public void setGroups(Player player, Scoreboard scoreboard) {
        setupGroups(player);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            PlayerManager playerManager = LuckPrefix.getInstance().getPlayerManager();
            Team team = scoreboard.getTeam(this.groupID.get(playerManager.getUserGroups().get(onlinePlayer.getUniqueId()))
                    + playerManager.getUserGroups().get(onlinePlayer.getUniqueId()));
            if (team == null) {
                Team defaultRole = scoreboard.getTeam(this.groupID.get("default") + "default");
                if (defaultRole == null) {
                    scoreboard.getTeams().forEach(Team::unregister);
                    for (Group loadedGroup : LuckPrefix.getInstance().getLuckPerms().getGroupManager().getLoadedGroups()) {
                        createGroup(loadedGroup.getName());
                    }
                    setupGroups(onlinePlayer);
                    defaultRole = scoreboard.getTeam(this.groupID.get("default") + "default");
                    if (defaultRole == null) {
                        continue;
                    }
                    defaultRole.addEntry(onlinePlayer.getName());
                    LuckPrefix.getInstance().getPlayerManager().setPlayerListName(onlinePlayer.getUniqueId());
                    continue;
                }
                defaultRole.addEntry(onlinePlayer.getName());
                continue;
            }
            Team currentTeam = scoreboard.getEntryTeam(onlinePlayer.getName());
            if (currentTeam == null) {
                team.addEntry(onlinePlayer.getName());
                continue;
            }
            if (currentTeam != team) {
                currentTeam.removeEntry(onlinePlayer.getName());
            }
            if (team.getEntries().contains(onlinePlayer.getName())) {
                continue;
            }
            team.addEntry(onlinePlayer.getName());
        }
    }

    public void createGroup(String group) {
        FileConfiguration config = LuckPrefix.getInstance().getGroupsFile().getConfiguration();
        this.groups.add(group);
        if (config.get(group) == null) {
            LuckPrefix.getInstance().getLogger().warning("Group values of `" + group + "` can't be loaded. Please check the groups.yml config!");
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
            Team team = scoreboard.getTeam(this.groupID.get(group) + group);
            if (team != null) {
                team.unregister();
            }
            team = scoreboard.registerNewTeam(this.groupID.get(group) + group);
            if (this.groupPrefix.get(group) != null && this.getGroupTabformat().get(group).contains("<prefix>")) {
                team.setPrefix(new Text(this.groupTabformat.get(group)
                        .replace("<prefix>", this.groupPrefix.get(group))
                        .replace("<player>", "")
                        .replace("<suffix>", "")).legacyMiniMessage());
            }
            if (this.groupSuffix.get(group) != null && this.getGroupTabformat().get(group).contains("<suffix>")) {
                team.setSuffix(" " + new Text(this.groupSuffix.get(group)).legacyMiniMessage());
            }
            if (this.groupColor.get(group) != null) {
                team.setColor(this.groupColor.get(group));
            }
        }
    }

    public void reloadGroup(String group) {
        deleteGroup(group);
        createGroup(group);
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
    
    public void loadGroups() {
        LuckPrefix instance = LuckPrefix.getInstance();
        for (Group group : instance.getLuckPerms().getGroupManager().getLoadedGroups()) {
            if (instance.getGroupsFile().contains(group.getName())) {
                instance.getGroupManager().createGroup(group.getName());
                continue;
            }
            if (instance.getConfig().getBoolean("Warning-If-Group-Can-Not-Loaded")) {
                instance.getLogger().warning("Group '" + group.getName() + "' can't be loaded.");
            }
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                instance.getPlayerManager().setPlayerListName(player.getUniqueId());
            }
        }, 1, instance.getConfig().getLong("UpdateTime") * 20);
    }
}
