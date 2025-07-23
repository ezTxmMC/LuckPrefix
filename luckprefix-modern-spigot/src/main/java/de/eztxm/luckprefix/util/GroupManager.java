package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.util.database.Processor;
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
            String sortId = this.groupID.get(group);
            if (sortId == null) {
                sortId = this.groupID.get("default");
                group = "default";
            }
            Team team = scoreboard.getTeam(sortId + group);
            while (team == null) {
                resetTeams(scoreboard);
                setupGroups(onlinePlayer);
                team = scoreboard.getTeam(this.groupID.get("default") + "default");
            }
            Team currentTeam = scoreboard.getEntryTeam(onlinePlayer.getName());
            if (currentTeam != team) {
                if (currentTeam != null) {
                    currentTeam.removeEntry(onlinePlayer.getName());
                }
                team.addEntry(onlinePlayer.getName());
            }
            this.instance.getPlayerManager().setPlayerListName(
                    playerId,
                    Objects.requireNonNull(this.instance.getLuckPerms().getUserManager().getUser(playerId)).getPrimaryGroup()
            );
        }
    }

    public void createGroup(String group) {
        if (this.instance.getDatabaseFile().getValue("Database.Enabled").asBoolean()) {
            /* TODO: Database Integration
             *
             *  - Remove return ?
             *  - Switch Case if database or config.
             *
             * */
            return;
        }
        FileConfiguration config = this.instance.getGroupsFile().getConfiguration();
        if (this.instance.getConfig().getBoolean("Auto-Add-Group")) {
            this.setIfNull(config, group + ".Prefix", "<gray>" + group);
            this.setIfNull(config, group + ".Suffix", "");
            this.setIfNull(config, group + ".Tabformat", "<prefix> <dark_gray>| <gray><player>");
            this.setIfNull(config, group + ".Chatformat", "<prefix> <dark_gray>- <gray><player><dark_gray> » <gray><message>");
            this.setIfNull(config, group + ".SortID", 90);
            this.setIfNull(config, group + ".NameColor", "gray");
        }
        if (config.get(group) == null) {
            if (this.instance.getConfig().getBoolean("Warning-If-Group-Can-Not-Loaded")) {
                this.instance.getLogger().warning("Group values of `" + group + "` can't be loaded. Please check the groups.yml config!");
            }
            return;
        }
        this.groups.add(group);
        this.groupPrefix.put(group, config.getString(group + ".Prefix"));
        this.groupSuffix.put(group, config.getString(group + ".Suffix"));
        this.groupTabformat.put(group, config.getString(group + ".Tabformat"));
        this.groupChatformat.put(group, config.getString(group + ".Chatformat"));
        String sortIDraw = String.valueOf(config.getInt(group + ".SortID")); // ex: 99
        int maxLength = 4;
        int currentLength = sortIDraw.length();
        String sortIDBuilt = "0".repeat(Math.max(0, maxLength - currentLength)) + sortIDraw; // ex: 0099 = 4 digit
        this.groupID.put(group, sortIDBuilt);
        try {
            ChatColor nameColor = ChatColor.valueOf(Objects.requireNonNull(config.getString(group + ".NameColor")).toUpperCase());
            this.groupColor.put(group, nameColor);
        } catch (IllegalArgumentException e) {
            this.groupColor.put(group, ChatColor.GRAY);
            this.instance.getLogger().warning("Can't find name color. Set to default GRAY.");
        }
    }

    public void setupGroups(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        for (String group : this.groups) {
            Team team = scoreboard.getTeam(this.groupID.get(group) + group); // ex: 0099default
            if (team != null) {
                team.unregister();
            }
            team = scoreboard.registerNewTeam(this.groupID.get(group) + group); // ex: 0099default
            if (this.getGroupTabformat().get(group) != null) {
                if (this.groupPrefix.get(group) != null && this.getGroupTabformat().get(group).contains("<prefix>")) {
                    team.setPrefix(new Text(this.groupTabformat.get(group)
                            .replace("<prefix>", this.groupPrefix.get(group))
                            .replace("<player>", "")
                            .replace("<suffix>", "")).legacyMiniMessage());
                }
                if (this.groupSuffix.get(group) != null && this.getGroupTabformat().get(group).contains("<suffix>")) {
                    team.setSuffix(new Text(this.groupSuffix.get(group)).legacyMiniMessage());
                }
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
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.instance.getPlayerManager().setPlayerListName(
                        player.getUniqueId(),
                        Objects.requireNonNull(this.instance.getLuckPerms().getUserManager().getUser(player.getUniqueId())).getPrimaryGroup()
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

    private void setIfNull(FileConfiguration configuration, String key, Object value) {
        if (configuration.get(key) != null) {
            return;
        }
        configuration.set(key, value);
    }
}
