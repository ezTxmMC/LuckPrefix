package de.eztxm.luckprefix.util;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.util.database.Processor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.model.group.Group;

@Getter
public class GroupManager {

    private final LuckPrefix instance;
    private final List<String> groups;
    private final Map<String, String> groupPrefix;
    private final Map<String, String> groupSuffix;
    private final Map<String, String> groupTabformat;
    private final Map<String, String> groupChatformat;
    private final Map<String, String> groupID;
    private final Map<String, NamedTextColor> groupColor;
    private final Map<String, Boolean> tried;

    public GroupManager(LuckPrefix instance) {
        this.instance = instance;
        this.groups = new ArrayList<>();
        this.groupPrefix = new HashMap<>();
        this.groupSuffix = new HashMap<>();
        this.groupTabformat = new HashMap<>();
        this.groupChatformat = new HashMap<>();
        this.groupID = new HashMap<>();
        this.groupColor = new HashMap<>();
        this.tried = new HashMap<>();
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
                        Objects.requireNonNull(this.instance.getLuckPerms().getUserManager().getUser(playerId)).getPrimaryGroup()
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
                if (this.instance.getConfig().getBoolean("Auto-Add-Group")) {
                    String message = "Values of group `" + group + "` has been added to your database!";
                    this.instance.getLogger()
                            .warning(message);
                    processor.addGroup(
                            group,
                            "<gray>Player",
                            "",
                            "<prefix> <dark_gray>- <gray><player><dark_gray> » <gray><message>",
                            "<prefix> <dark_gray>| <gray><player>",
                            999,
                            "GRAY");
                    createGroup(group);
                    return;
                }
                String message = "Group values of `" + group + "` can't be loaded. Please check your database entries!";
                this.instance.getLogger().warning(message);
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
            this.groupColor.put(group,
                    Text.fromString(processor.getGroupValue(group, "namecolor").asString().toUpperCase()));
            return;
        }
        FileConfiguration config = this.instance.getGroupsFile().getConfiguration();
        this.groups.add(group);
        if (config.get(group) == null) {
            if (this.instance.getConfig().getBoolean("Auto-Add-Group")) {
                config.set(group + ".Prefix", "<gray>Player");
                config.set(group + ".Suffix", "");
                config.set(group + ".Tabformat", "<prefix> <dark_gray>| <gray><player>");
                config.set(group + ".Chatformat", "<prefix> <dark_gray>- <gray><player><dark_gray> » <gray><message>");
                config.set(group + ".SortID", 999);
                config.set(group + ".NameColor", "GRAY");
                createGroup(group);
                return;
            }
            String message = "Group values of `" + group + "` can't be loaded. Please check the groups.yml config!";
            this.instance.getLogger()
                    .warning(message);
            return;
        }
        if (config.get(group + ".Prefix") == null) {
            config.set(group + ".Prefix", "<gray>Player");
        }
        if (config.get(group + ".Suffix") == null) {
            config.set(group + ".Suffix", "");
        }
        if (config.get(group + ".Tabformat") == null) {
            config.set(group + ".Tabformat", "<prefix> <dark_gray>| <gray><player>");
        }
        if (config.get(group + ".Chatformat") == null) {
            config.set(group + ".Chatformat", "<prefix> <dark_gray>- <gray><player><dark_gray> » <gray><message>");
        }
        if (config.get(group + ".SortID") == null) {
            config.set(group + ".SortID", 999);
        }
        if (config.get(group + ".NameColor") == null) {
            config.set(group + ".NameColor", "GRAY");
        }
        this.instance.getGroupsFile().saveConfiguration();
        this.instance.getGroupsFile().reloadConfig();
        this.groupPrefix.put(group, config.getString(group + ".Prefix"));
        this.groupSuffix.put(group, config.getString(group + ".Suffix"));
        this.groupTabformat.put(group, config.getString(group + ".Tabformat"));
        this.groupChatformat.put(group, config.getString(group + ".Chatformat"));
        String sortIDraw = String.valueOf(config.getInt(group + ".SortID"));
        int maxLength = 4;
        int currentLength = sortIDraw.length();
        String sortIDBuilt = "0".repeat(Math.max(0, maxLength - currentLength)) + sortIDraw;
        this.groupID.put(group, sortIDBuilt);
        this.groupColor.put(group, Text.fromString(config.getString(group + ".NameColor").toUpperCase()));
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
                team.prefix(new Text(this.groupTabformat.get(group)
                        .replace("<prefix>", this.groupPrefix.get(group))
                        .replace("<player>", "")
                        .replace("<suffix>", "")).miniMessage());
            }
            if (this.groupSuffix.get(group) != null && this.getGroupTabformat().get(group).contains("<suffix>")) {
                team.suffix(Component.text(" ").append(new Text(this.groupSuffix.get(group)).miniMessage()));
            }
            if (this.groupColor.get(group) != null) {
                team.color(this.groupColor.get(group));
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
                String message = "Group '" + group.getName() + "' can't be loaded.";
                this.instance.getLogger().warning(message);
            }
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(this.instance, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.instance.getPlayerManager().setPlayerListName(
                        player.getUniqueId(),
                        this.instance.getLuckPerms().getUserManager().getUser(player.getUniqueId()).getPrimaryGroup());
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
