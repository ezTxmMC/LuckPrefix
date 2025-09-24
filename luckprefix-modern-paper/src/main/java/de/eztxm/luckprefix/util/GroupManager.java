package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.DatabaseConfig;
import de.eztxm.luckprefix.common.config.GroupsConfig;
import de.eztxm.luckprefix.common.config.MainConfig;
import de.eztxm.luckprefix.common.util.Encoder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class GroupManager {

    private static final int TEAM_NAME_MAX_LENGTH = 16;

    private final LuckPrefix plugin;
    private final List<String> loadedGroups = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> prefixByGroup = new ConcurrentHashMap<>();
    private final Map<String, String> suffixByGroup = new ConcurrentHashMap<>();
    private final Map<String, String> tabFormatByGroup = new ConcurrentHashMap<>();
    private final Map<String, String> chatFormatByGroup = new ConcurrentHashMap<>();
    private final Map<String, String> sortIdByGroup = new ConcurrentHashMap<>();
    private final Map<String, NamedTextColor> nameColorByGroup = new ConcurrentHashMap<>();

    public GroupManager(LuckPrefix plugin) {
        this.plugin = plugin;
    }

    public void setGroups(Player player, Scoreboard scoreboard) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(this.plugin, () -> setGroups(player, scoreboard));
            return;
        }

        setupGroups(player);

        var playerManager = plugin.getPlayerManager();
        for (Player online : Bukkit.getOnlinePlayers()) {
            UUID playerId = online.getUniqueId();
            String groupName = playerManager.getUserGroups().get(playerId);
            String paddedSortId = sortIdByGroup.get(groupName);

            if (paddedSortId == null) {
                paddedSortId = sortIdByGroup.get("default");
                groupName = "default";
            }

            String teamName = buildTeamKey(groupName);
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                setupGroups(online);
                team = scoreboard.getTeam(buildTeamKey("default"));
            }

            if (team != null) {
                Team currentTeam = scoreboard.getEntryTeam(online.getName());
                if (currentTeam != team) {
                    if (currentTeam != null) {
                        try {
                            currentTeam.removeEntry(online.getName());
                        } catch (IllegalStateException ignored) {
                        }
                    }
                    try {
                        team.addEntry(online.getName());
                    } catch (IllegalStateException ex) {
                        plugin.getLogger().warning("Failed to add player to team: " + ex.getMessage());
                    }
                }
            }

            plugin.getPlayerManager().setPlayerListName(
                    playerId,
                    Objects.requireNonNull(plugin.getLuckPerms().getUserManager().getUser(playerId)).getPrimaryGroup()
            );
        }
    }

    public void createGroup(String rawGroupName) {
        ConfigService configService = plugin.getConfigService();
        DatabaseConfig databaseConfig = configService.of(DatabaseConfig.class);
        if (databaseConfig.isDatabaseEnabled()) {
            // TODO: Database Integration (currently skip local YAML path)
            return;
        }

        MainConfig mainConfig = configService.of(MainConfig.class);
        GroupsConfig groupsConfig = configService.of(GroupsConfig.class);

        boolean autoAdd = mainConfig.isAutoAddGroupEnabled();
        boolean warnIfMissing = mainConfig.isWarnIfGroupCannotBeLoaded();

        if (rawGroupName.contains(":") && warnIfMissing) {
            plugin.getLogger().warning("Group name '" + rawGroupName + "' contains ':' — will be stored as '" +
                    Encoder.key(rawGroupName) + "' in groups.yml.");
        }

        groupsConfig.ensureGroupDefaults(rawGroupName, autoAdd, msg -> plugin.getLogger().warning(msg));

        String prefix = groupsConfig.getPrefix(rawGroupName);
        String suffix = groupsConfig.getSuffix(rawGroupName);
        String tabFormat = groupsConfig.getTabFormat(rawGroupName);
        String chatFormat = groupsConfig.getChatFormat(rawGroupName);

        int sortId = groupsConfig.getSortId(rawGroupName);
        if (sortId < 0) sortId = 0;
        String paddedSortId = String.format("%04d", sortId);

        NamedTextColor nameColor = NamedTextColor.GRAY;
        String colorName = groupsConfig.getNameColor(rawGroupName);
        if (colorName != null) {
            try {
                nameColor = Text.fromString(colorName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("groups.yml: '" + rawGroupName + ".NameColor' = '" + colorName + "' is invalid — falling back to GRAY.");
            }
        }

        loadedGroups.add(rawGroupName);
        prefixByGroup.put(rawGroupName, prefix == null ? "" : prefix);
        suffixByGroup.put(rawGroupName, suffix == null ? "" : suffix);
        tabFormatByGroup.put(rawGroupName, tabFormat == null ? "" : tabFormat);
        chatFormatByGroup.put(rawGroupName, chatFormat == null ? "" : chatFormat);
        sortIdByGroup.put(rawGroupName, paddedSortId);
        nameColorByGroup.put(rawGroupName, nameColor);
    }

    public void reloadGroup(String rawGroupName) {
        deleteGroup(rawGroupName);
        createGroup(rawGroupName);
    }

    public void deleteGroup(String rawGroupName) {
        loadedGroups.remove(rawGroupName);
        prefixByGroup.remove(rawGroupName);
        suffixByGroup.remove(rawGroupName);
        tabFormatByGroup.remove(rawGroupName);
        chatFormatByGroup.remove(rawGroupName);
        sortIdByGroup.remove(rawGroupName);
        nameColorByGroup.remove(rawGroupName);
    }

    public void reloadAllFromConfigs() {
        clearAllCaches();

        var lpGroups = plugin.getLuckPerms().getGroupManager().getLoadedGroups();

        for (Group lpGroup : lpGroups) {
            String rawName = lpGroup.getName();
            createGroup(rawName);
        }

        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getPlayerManager().setPlayerListName(
                        player.getUniqueId(),
                        Objects.requireNonNull(plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId())).getPrimaryGroup()
                );
            }
        }
    }

    public void loadGroups() {
        var groups = plugin.getLuckPerms().getGroupManager().getLoadedGroups();
        var groupsConfig = plugin.getConfigService().of(GroupsConfig.class);
        var mainConfig = plugin.getConfigService().of(MainConfig.class);

        for (Group group : groups) {
            String name = group.getName();
            boolean exists = groupsConfig.hasGroup(name);

            if (exists) {
                createGroup(name);
                continue;
            }

            if (mainConfig.isWarnIfGroupCannotBeLoaded()) {
                plugin.getLogger().warning("Group '" + name + "' can't be loaded (not found in groups.yml).");
            }
        }

        long updateTimeSeconds = Math.max(1L, mainConfig.getUpdateTimeSeconds());
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getPlayerManager().setPlayerListName(
                        player.getUniqueId(),
                        Objects.requireNonNull(plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId())).getPrimaryGroup()
                );
            }
        }, 1L, updateTimeSeconds * 20L);
    }

    public void setupGroups(Player player) {
        synchronized (this) {
            Scoreboard scoreboard = player.getScoreboard();

            for (String rawGroupName : new ArrayList<>(loadedGroups)) {
                String teamName = buildTeamKey(rawGroupName);
                Team team = scoreboard.getTeam(teamName);

                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName);
                }

                String tabFormat = tabFormatByGroup.get(rawGroupName);
                if (tabFormat != null) {
                    String prefix = prefixByGroup.get(rawGroupName);
                    if (prefix != null && tabFormat.contains("<prefix>")) {
                        try {
                            Component teamPrefix = new Text(tabFormat
                                    .replace("<prefix>", prefix)
                                    .replace("<suffix>", "")
                                    .replace("<player>", "")).placeholders(player).miniMessage();
                            team.prefix(teamPrefix);
                        } catch (Exception ex) {
                            plugin.getLogger().warning("Error setting team prefix for group " + rawGroupName + ": " + ex.getMessage());
                        }
                    }

                    String suffix = suffixByGroup.get(rawGroupName);
                    if (suffix != null && tabFormat.contains("<suffix>")) {
                        try {
                            Component teamSuffix = new Text(suffix).placeholders(player).miniMessage();
                            team.suffix(teamSuffix);
                        } catch (Exception ex) {
                            plugin.getLogger().warning("Error setting team suffix for group " + rawGroupName + ": " + ex.getMessage());
                        }
                    }
                }

                NamedTextColor nameColor = nameColorByGroup.get(rawGroupName);
                if (nameColor != null) {
                    try {
                        team.color(nameColor);
                    } catch (Exception ex) {
                        plugin.getLogger().warning("Error setting team color for group " + rawGroupName + ": " + ex.getMessage());
                    }
                }
            }
        }
    }

    private void clearAllCaches() {
        loadedGroups.clear();
        prefixByGroup.clear();
        suffixByGroup.clear();
        tabFormatByGroup.clear();
        chatFormatByGroup.clear();
        sortIdByGroup.clear();
        nameColorByGroup.clear();
    }

    private String buildTeamKey(String rawGroupName) {
        String padded = sortIdByGroup.getOrDefault(rawGroupName, "9999");
        String key = padded + Encoder.key(rawGroupName);
        key = key.replace(' ', '_');
        if (key.length() > TEAM_NAME_MAX_LENGTH) {
            key = key.substring(0, TEAM_NAME_MAX_LENGTH);
        }
        return key;
    }

}
