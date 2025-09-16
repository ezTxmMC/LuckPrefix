package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    public GroupManager(LuckPrefix instance) {
        this.instance = instance;
        this.groups = Collections.synchronizedList(new ArrayList<>());
        this.groupPrefix = new ConcurrentHashMap<>();
        this.groupSuffix = new ConcurrentHashMap<>();
        this.groupTabformat = new ConcurrentHashMap<>();
        this.groupChatformat = new ConcurrentHashMap<>();
        this.groupID = new ConcurrentHashMap<>();
        this.groupColor = new ConcurrentHashMap<>();
    }

    public void setGroups(Player player, Scoreboard scoreboard) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(this.instance, () -> setGroups(player, scoreboard));
            return;
        }
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
            String teamName = sortId + group;
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                setupGroups(onlinePlayer);
                team = scoreboard.getTeam(this.groupID.get("default") + "default");
            }
            if (team != null) {
                Team currentTeam = scoreboard.getEntryTeam(onlinePlayer.getName());
                if (currentTeam != team) {
                    if (currentTeam != null) {
                        try {
                            currentTeam.removeEntry(onlinePlayer.getName());
                        } catch (IllegalStateException ignored) {}
                    }
                    try {
                        team.addEntry(onlinePlayer.getName());
                    } catch (IllegalStateException e) {
                        this.instance.getLogger().warning("Failed to add player to team: " + e.getMessage());
                    }
                }
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
            this.groupColor.put(group, Text.fromString(config.getString(group + ".NameColor").toUpperCase()));
        } catch (IllegalArgumentException e) {
            this.groupColor.put(group, NamedTextColor.GRAY);
            this.instance.getLogger().warning("Can't find name color. Set to default GRAY.");
        }
    }

    public void setupGroups(Player player) {
        synchronized (this) {
            Scoreboard scoreboard = player.getScoreboard();

            for (String group : new ArrayList<>(this.groups)) {
                String teamName = this.groupID.get(group) + group;
                Team team = scoreboard.getTeam(teamName);

                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName);
                }

                String tabFormat = this.getGroupTabformat().get(group);
                if (tabFormat != null) {
                    String prefix = this.groupPrefix.get(group);
                    if (prefix != null && tabFormat.contains("<prefix>")) {
                        try {
                            team.prefix(new Text(tabFormat
                                    .replace("<prefix>", prefix)
                                    .replace("<suffix>", "")
                                    .replace("<player>", "")).placeholders(player).miniMessage());
                        } catch (Exception e) {
                            this.instance.getLogger().warning("Error setting team prefix for group " + group + ": " + e.getMessage());
                        }
                    }

                    String suffix = this.groupSuffix.get(group);
                    if (suffix != null && tabFormat.contains("<suffix>")) {
                        try {
                            team.suffix(new Text(suffix).placeholders(player).miniMessage());
                        } catch (Exception e) {
                            this.instance.getLogger().warning("Error setting team suffix for group " + group + ": " + e.getMessage());
                        }
                    }
                }

                NamedTextColor color = this.groupColor.get(group);
                if (color != null) {
                    try {
                        team.color(color);
                    } catch (Exception e) {
                        this.instance.getLogger().warning("Error setting team color for group " + group + ": " + e.getMessage());
                    }
                }
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
        Bukkit.getScheduler().runTaskTimer(this.instance, () -> {
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

    private void setIfNull(FileConfiguration configuration, String key, Object value) {
        if (configuration.get(key) != null) {
            return;
        }
        configuration.set(key, value);
    }
}
