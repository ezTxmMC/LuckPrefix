package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.database.Processor;
import lombok.Getter;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

@Getter
public class GroupManager {
    private final LuckPrefix instance;
    private final List<String> groups;
    private final Map<String, String> groupPrefix;
    private final Map<String, String> groupSuffix;
    private final Map<String, String> groupTabformat;
    private final Map<String, String> groupChatformat;
    private final Map<String, String> groupID;
    private final Map<String, ChatColor> groupColor;

    public GroupManager(LuckPrefix instance) {
        this.instance = instance;
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
        PlayerManager playerManager = this.instance.getPlayerManager();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            UUID playerId = onlinePlayer.getUniqueId();
            String group = playerManager.getUserGroups().get(playerId);
            String teamName = this.groupID.getOrDefault(group, "default") + group;
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                resetTeams(scoreboard);
                setupGroups(onlinePlayer);
                team = scoreboard.getTeam(this.groupID.get("default") + "default");
            }
            if (team != null) {
                Team currentTeam = scoreboard.getEntryTeam(onlinePlayer.getName());
                if (currentTeam != team) {
                    if (currentTeam != null) {
                        currentTeam.removeEntry(onlinePlayer.getName());
                    }
                    team.addEntry(onlinePlayer.getName());
                }
                this.instance.getPlayerManager().setPlayerListName(
                        playerId,
                        this.instance.getLuckPerms().getUserManager().getUser(player.getUniqueId()).getPrimaryGroup()
                        );
            }
        }
    }

    public void createGroup(String group) {
        if (this.instance.getDatabaseFile().getValue("Database.Enabled").asBoolean()) {
            Processor processor = DatabaseHandler.selectProcessor();
            if (processor == null) {
                this.instance.getLogger().warning("No database processor selected.");
                return;
            }
            this.groups.add(group);
            if (!processor.isGroupExists(group)) {
                this.instance.getLogger().warning("Group values of `" + group + "` can't be loaded. Please check your database table!");
                return;
            }
            this.groupPrefix.put(group, processor.getGroupValue(group, "prefix").asString());
            this.groupSuffix.put(group, processor.getGroupValue(group, "suffix").asString());
            this.groupTabformat.put(group, processor.getGroupValue(group, "tabformat").asString());
            this.groupChatformat.put(group, processor.getGroupValue(group, "chatformat").asString());
            String sortIDraw = String.valueOf(processor.getGroupValue(group, "sortId").asInteger());
            int maxLength = 4;
            int currentLength = sortIDraw.length();
            String sortIDBuilt = "0".repeat(Math.max(0, maxLength - currentLength)) + sortIDraw;
            this.groupID.put(group, sortIDBuilt);
            this.groupColor.put(group, ChatColor.valueOf(processor.getGroupValue(group, "namecolor").asString().toUpperCase()));
            return;
        }
        FileConfiguration config = this.instance.getGroupsFile().getConfiguration();
        this.groups.add(group);
        if (config.get(group) == null) {
            this.instance.getLogger().warning("Group values of `" + group + "` can't be loaded. Please check the groups.yml config!");
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
        for (Group group : this.instance.getLuckPerms().getGroupManager().getLoadedGroups()) {
            if (this.instance.getGroupsFile().contains(group.getName())) {
                this.instance.getGroupManager().createGroup(group.getName());
                continue;
            }
            if (this.instance.getConfig().getBoolean("Warning-If-Group-Can-Not-Loaded")) {
                this.instance.getLogger().warning("Group '" + group.getName() + "' can't be loaded.");
            }
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(this.instance, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.instance.getPlayerManager().setPlayerListName(
                        player.getUniqueId(),
                        this.instance.getLuckPerms().getUserManager().getUser(player.getUniqueId()).getPrimaryGroup()
                );
            }
        }, 1, this.instance.getConfig().getLong("UpdateTime") * 20);
    }

    private void resetTeams(Scoreboard scoreboard) {
        scoreboard.getTeams().forEach(Team::unregister);
        for (Group loadedGroup : this.instance.getLuckPerms().getGroupManager().getLoadedGroups()) {
            createGroup(loadedGroup.getName());
        }
    }
}
