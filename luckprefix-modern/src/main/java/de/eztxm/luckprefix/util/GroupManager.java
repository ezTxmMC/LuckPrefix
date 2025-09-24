package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.DatabaseConfig;
import de.eztxm.luckprefix.common.config.GroupsConfig;
import de.eztxm.luckprefix.common.config.MainConfig;
import de.eztxm.luckprefix.common.util.Encoder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class GroupManager {

    private static final int TEAM_NAME_MAX_LENGTH = 16;

    private final LuckPrefix plugin;

    private final List<String> loadedGroups = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, String> prefixByGroup = new ConcurrentHashMap<>();
    private final Map<String, String> suffixByGroup = new ConcurrentHashMap<>();
    private final Map<String, String> tabFormatByGroup = new ConcurrentHashMap<>();
    private final Map<String, String> chatFormatByGroup = new ConcurrentHashMap<>();
    private final Map<String, String> sortIdByGroup = new ConcurrentHashMap<>();
    private final Map<String, ChatColor> nameColorByGroup = new ConcurrentHashMap<>();

    public GroupManager(LuckPrefix plugin) {
        this.plugin = plugin;
    }

    public void loadGroups() {
        ConfigService configService = plugin.getConfigService();
        MainConfig mainConfig = configService.of(MainConfig.class);
        GroupsConfig groupsConfig = configService.of(GroupsConfig.class);

        for (var lpGroup : plugin.getLuckPerms().getGroupManager().getLoadedGroups()) {
            String rawGroupName = lpGroup.getName();
            if (groupsConfig.hasGroup(rawGroupName)) {
                createGroup(rawGroupName);
                return;
            }
            if (mainConfig.isWarnIfGroupCannotBeLoaded()) {
                plugin.getLogger().warning("Group '" + rawGroupName + "' can't be loaded (not found in groups.yml).");
            }
        }

        long updateIntervalSeconds = Math.max(1L, mainConfig.getUpdateTimeSeconds());
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) return;
            for (Player online : Bukkit.getOnlinePlayers()) {
                plugin.getPlayerManager().setPlayerListName(
                        online.getUniqueId(),
                        Objects.requireNonNull(plugin.getLuckPerms().getUserManager().getUser(online.getUniqueId())).getPrimaryGroup()
                );
            }
        }, 1L, updateIntervalSeconds * 20L);
    }

    public void reloadAllFromConfigs() {
        clearAllCaches();
        for (var lpGroup : plugin.getLuckPerms().getGroupManager().getLoadedGroups()) {
            createGroup(lpGroup.getName());
        }
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                plugin.getPlayerManager().setPlayerListName(
                        online.getUniqueId(),
                        Objects.requireNonNull(plugin.getLuckPerms().getUserManager().getUser(online.getUniqueId())).getPrimaryGroup()
                );
            }
        }
    }

    public void reloadGroup(String rawGroupName) {
        deleteGroup(rawGroupName);
        createGroup(rawGroupName);
    }

    public void createGroup(String rawGroupName) {
        ConfigService configService = plugin.getConfigService();
        DatabaseConfig databaseConfig = configService.of(DatabaseConfig.class);
        if (databaseConfig.isDatabaseEnabled()) {
            // TODO: DB integration
            return;
        }

        MainConfig mainConfig = configService.of(MainConfig.class);
        GroupsConfig groupsConfig = configService.of(GroupsConfig.class);

        boolean autoAddGroup = mainConfig.isAutoAddGroupEnabled();
        boolean warnIfMissing = mainConfig.isWarnIfGroupCannotBeLoaded();

        if (rawGroupName.contains(":") && warnIfMissing) {
            plugin.getLogger().warning("Group name '" + rawGroupName + "' contains ':' — will be stored as '" +
                    Encoder.key(rawGroupName) + "' in groups.yml.");
        }

        groupsConfig.ensureGroupDefaults(rawGroupName, autoAddGroup, plugin.getLogger()::warning);

        String prefix = groupsConfig.getPrefix(rawGroupName);
        String suffix = groupsConfig.getSuffix(rawGroupName);
        String tabFormat = groupsConfig.getTabFormat(rawGroupName);
        String chatFormat = groupsConfig.getChatFormat(rawGroupName);

        int sortId = groupsConfig.getSortId(rawGroupName);
        if (sortId < 0) sortId = 0;
        String paddedSortId = String.format("%04d", sortId);

        ChatColor chatColor = ChatColor.GRAY;
        String colorName = groupsConfig.getNameColor(rawGroupName);
        if (colorName != null) {
            try {
                chatColor = ChatColor.valueOf(colorName.trim().toUpperCase());
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
        nameColorByGroup.put(rawGroupName, chatColor);
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

    public void setGroups(Player player, Scoreboard scoreboard) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> setGroups(player, scoreboard));
            return;
        }

        setupGroups(player);

        for (Player online : Bukkit.getOnlinePlayers()) {
            String currentGroup = plugin.getPlayerManager().getUserGroups().get(online.getUniqueId());
            String sortId = sortIdByGroup.get(currentGroup);

            if (sortId == null) {
                sortId = sortIdByGroup.get("default");
                currentGroup = "default";
            }

            String teamKey = buildTeamKey(currentGroup);
            Team team = scoreboard.getTeam(teamKey);
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
                    online.getUniqueId(),
                    Objects.requireNonNull(plugin.getLuckPerms().getUserManager().getUser(online.getUniqueId())).getPrimaryGroup()
            );
        }
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
                            String legacyPrefix = new Text(
                                    tabFormat.replace("<prefix>", prefix)
                                            .replace("<suffix>", "")
                                            .replace("<player>", "")
                            ).placeholders(player).legacyMiniMessage();
                            team.setPrefix(legacyPrefix);
                        } catch (Exception ex) {
                            plugin.getLogger().warning("Error setting team prefix for group " + rawGroupName + ": " + ex.getMessage());
                        }
                    }

                    String suffix = suffixByGroup.get(rawGroupName);
                    if (suffix != null && tabFormat.contains("<suffix>")) {
                        try {
                            String legacySuffix = new Text(suffix).placeholders(player).legacyMiniMessage();
                            team.setSuffix(legacySuffix);
                        } catch (Exception ex) {
                            plugin.getLogger().warning("Error setting team suffix for group " + rawGroupName + ": " + ex.getMessage());
                        }
                    }
                }

                ChatColor teamColor = nameColorByGroup.get(rawGroupName);
                if (teamColor != null) {
                    try {
                        team.setColor(teamColor);
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
        String paddedSortId = sortIdByGroup.getOrDefault(rawGroupName, "9999");
        String key = paddedSortId + Encoder.key(rawGroupName);
        key = key.replace(' ', '_');
        if (key.length() > TEAM_NAME_MAX_LENGTH) {
            key = key.substring(0, TEAM_NAME_MAX_LENGTH);
        }
        return key;
    }
}
