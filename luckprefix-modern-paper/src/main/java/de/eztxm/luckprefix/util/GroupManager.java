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
public final class GroupManager {

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

    public void loadGroups() {
        plugin.getDebugLog().info("GroupManager.loadGroups: start");

        ConfigService configService = plugin.getConfigService();
        MainConfig mainConfig = configService.of(MainConfig.class);
        GroupsConfig groupsConfig = configService.of(GroupsConfig.class);

        Collection<Group> lpGroups = plugin.getLuckPerms().getGroupManager().getLoadedGroups();
        plugin.getDebugLog().debug("GroupManager.loadGroups: lpGroups=" + lpGroups.size());

        for (Group lpGroup : lpGroups) {
            String rawGroupName = lpGroup.getName();
            boolean exists = groupsConfig.hasGroup(rawGroupName);
            if (exists) {
                createGroup(rawGroupName);
                continue;
            }
            if (mainConfig.isWarnIfGroupCannotBeLoaded()) {
                plugin.getLogger().warning("Group '" + rawGroupName + "' can't be loaded (not found in groups.yml).");
            }
            plugin.getDebugLog().warn("Group missing in groups.yml: " + rawGroupName);
        }

        long updateSeconds = Math.max(1L, mainConfig.getUpdateTimeSeconds());
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                try {
                    plugin.getPlayerManager().setPlayerListName(
                            player.getUniqueId(),
                            Objects.requireNonNull(plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId()))
                                    .getPrimaryGroup()
                    );
                } catch (Exception ex) {
                    plugin.getDebugLog().error("Periodic list-name update failed for " + player.getName(), ex);
                }
            }
        }, 1L, updateSeconds * 20L);

        plugin.getDebugLog().info("GroupManager.loadGroups: done");
    }

    public void reloadAllFromConfigs() {
        plugin.getDebugLog().info("GroupManager.reloadAllFromConfigs: start");

        clearAllCaches();

        Collection<Group> lpGroups = plugin.getLuckPerms().getGroupManager().getLoadedGroups();
        plugin.getDebugLog().debug("GroupManager.reloadAllFromConfigs: lpGroups=" + lpGroups.size());

        for (Group lpGroup : lpGroups) {
            createGroup(lpGroup.getName());
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                plugin.getPlayerManager().setPlayerListName(
                        player.getUniqueId(),
                        Objects.requireNonNull(plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId()))
                                .getPrimaryGroup()
                );
            } catch (Exception ex) {
                plugin.getDebugLog().error("List-name update after reload failed for " + player.getName(), ex);
            }
        }

        refreshAllScoreboards();
        plugin.getDebugLog().info("GroupManager.reloadAllFromConfigs: done");
    }

    public void reloadGroup(String rawGroupName) {
        plugin.getDebugLog().info("GroupManager.reloadGroup: " + rawGroupName);
        deleteGroup(rawGroupName);
        createGroup(rawGroupName);
        refreshAllScoreboards();
    }

    public void deleteGroup(String rawGroupName) {
        plugin.getDebugLog().debug("GroupManager.deleteGroup: " + rawGroupName);
        loadedGroups.remove(rawGroupName);
        prefixByGroup.remove(rawGroupName);
        suffixByGroup.remove(rawGroupName);
        tabFormatByGroup.remove(rawGroupName);
        chatFormatByGroup.remove(rawGroupName);
        sortIdByGroup.remove(rawGroupName);
        nameColorByGroup.remove(rawGroupName);
    }

    public void createGroup(String rawGroupName) {
        plugin.getDebugLog().debug("GroupManager.createGroup: start raw=" + rawGroupName);

        ConfigService configService = plugin.getConfigService();

        DatabaseConfig databaseConfig = configService.of(DatabaseConfig.class);
        if (databaseConfig.isDatabaseEnabled()) {
            plugin.getDebugLog().info("GroupManager.createGroup: database mode enabled – skipping YAML path for " + rawGroupName);

            return;
        }

        MainConfig mainConfig = configService.of(MainConfig.class);
        GroupsConfig groupsConfig = configService.of(GroupsConfig.class);

        boolean autoAdd = mainConfig.isAutoAddGroupEnabled();
        boolean warnMissing = mainConfig.isWarnIfGroupCannotBeLoaded();

        if (rawGroupName.contains(":") && warnMissing) {
            String encoded = Encoder.key(rawGroupName);
            plugin.getLogger().warning("Group name '" + rawGroupName + "' contains ':' — stored as '" + encoded + "' in groups.yml.");
            plugin.getDebugLog().warn("Group contains colon, will use encoded key: " + encoded);
        }

        groupsConfig.ensureGroupDefaults(rawGroupName, autoAdd, mainConfig.isPrintWarningsEnabled(), msg -> {
            plugin.getLogger().warning(msg);
        });

        String prefix = groupsConfig.getPrefix(rawGroupName);
        String suffix = groupsConfig.getSuffix(rawGroupName);
        String tabFormat = groupsConfig.getTabFormat(rawGroupName);
        String chatFormat = groupsConfig.getChatFormat(rawGroupName);

        int sortId = groupsConfig.getSortId(rawGroupName);
        if (sortId < 0) sortId = 0;
        String paddedSortId = String.format("%04d", sortId);

        NamedTextColor color = NamedTextColor.GRAY;
        String colorName = groupsConfig.getNameColor(rawGroupName);
        if (colorName != null) {
            try {
                color = Text.fromString(colorName.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                String msg = "groups.yml: '" + rawGroupName + ".NameColor' = '" + colorName + "' invalid – fallback GRAY.";
                plugin.getLogger().warning(msg);
                plugin.getDebugLog().warn(msg);
            }
        }

        loadedGroups.add(rawGroupName);
        prefixByGroup.put(rawGroupName, prefix == null ? "" : prefix);
        suffixByGroup.put(rawGroupName, suffix == null ? "" : suffix);
        tabFormatByGroup.put(rawGroupName, tabFormat == null ? "" : tabFormat);
        chatFormatByGroup.put(rawGroupName, chatFormat == null ? "" : chatFormat);
        sortIdByGroup.put(rawGroupName, paddedSortId);
        nameColorByGroup.put(rawGroupName, color);

        plugin.getDebugLog().debug("GroupManager.createGroup: values: prefix=" + prefix + " suffix=" + suffix
                + " tab=" + tabFormat + " chat=" + chatFormat + " sortId=" + paddedSortId + " color=" + color);

        plugin.getDebugLog().info("GroupManager.createGroup: loaded '" + rawGroupName + "'");
    }

    public void setGroups(Player player, Scoreboard scoreboard) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> setGroups(player, scoreboard));
            return;
        }

        setupGroups(player);

        for (Player online : Bukkit.getOnlinePlayers()) {
            UUID playerId = online.getUniqueId();
            String groupName = plugin.getPlayerManager().getUserGroups().get(playerId);
            String paddedSortId = groupName == null ? null : sortIdByGroup.get(groupName);

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
                boolean sameTeam = currentTeam != null && currentTeam.getName().equals(team.getName());

                if (!sameTeam) {
                    if (currentTeam != null) {
                        try {
                            currentTeam.removeEntry(online.getName());
                        } catch (IllegalStateException ex) {
                            plugin.getDebugLog().warn("Failed to remove entry '" + online.getName() + "' from team '" + currentTeam.getName() + "': " + ex.getMessage());
                        }
                    }
                    try {
                        team.addEntry(online.getName());
                    } catch (IllegalStateException ex) {
                        plugin.getDebugLog().warn("Failed to add entry '" + online.getName() + "' to team '" + team.getName() + "': " + ex.getMessage());
                    }
                }
            }

            try {
                plugin.getPlayerManager().setPlayerListName(
                        playerId,
                        Objects.requireNonNull(plugin.getLuckPerms().getUserManager().getUser(playerId)).getPrimaryGroup()
                );
            } catch (Exception ex) {
                plugin.getDebugLog().error("setPlayerListName failed for " + online.getName(), ex);
            }
        }
    }

    public void setupGroups(Player viewer) {
        synchronized (this) {
            Scoreboard scoreboard = viewer.getScoreboard();

            for (String rawGroupName : new ArrayList<>(loadedGroups)) {
                String teamName = buildTeamKey(rawGroupName);
                Team team = scoreboard.getTeam(teamName);
                if (team == null) {
                    try {
                        team = scoreboard.registerNewTeam(teamName);
                        plugin.getDebugLog().debug("setupGroups: registered team '" + teamName + "' for viewer " + viewer.getName());
                    } catch (IllegalArgumentException ex) {
                        plugin.getDebugLog().warn("setupGroups: team already exists? '" + teamName + "': " + ex.getMessage());
                        continue;
                    }
                }

                String tabFormat = tabFormatByGroup.get(rawGroupName);
                if (tabFormat != null) {
                    String prefix = prefixByGroup.get(rawGroupName);
                    if (prefix != null && tabFormat.contains("<prefix>")) {
                        try {
                            Component comp = new Text(tabFormat
                                    .replace("<prefix>", prefix)
                                    .replace("<suffix>", "")
                                    .replace("<player>", "")).placeholders(viewer).miniMessage();
                            team.prefix(comp);
                        } catch (Exception ex) {
                            plugin.getDebugLog().error("setupGroups: prefix build failed for group " + rawGroupName, ex);
                        }
                    }

                    String suffix = suffixByGroup.get(rawGroupName);
                    if (suffix != null && tabFormat.contains("<suffix>")) {
                        try {
                            Component comp = new Text(suffix).placeholders(viewer).miniMessage();
                            team.suffix(comp);
                        } catch (Exception ex) {
                            plugin.getDebugLog().error("setupGroups: suffix build failed for group " + rawGroupName, ex);
                        }
                    }
                }

                NamedTextColor color = nameColorByGroup.get(rawGroupName);
                if (color != null) {
                    try {
                        team.color(color);
                    } catch (Exception ex) {
                        plugin.getDebugLog().error("setupGroups: team.color failed for group " + rawGroupName, ex);
                    }
                }
            }
        }
    }

    public void refreshAllScoreboards() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::refreshAllScoreboards);
            return;
        }
        plugin.getDebugLog().debug("GroupManager.refreshAllScoreboards: main thread");

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            try {
                setupGroups(viewer);
            } catch (Exception ex) {
                plugin.getDebugLog().error("refreshAllScoreboards: setupGroups failed for viewer " + viewer.getName(), ex);
            }
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            try {
                setGroups(viewer, viewer.getScoreboard());
            } catch (Exception ex) {
                plugin.getDebugLog().error("refreshAllScoreboards: setGroups failed for viewer " + viewer.getName(), ex);
            }
        }
        plugin.getDebugLog().info("GroupManager.refreshAllScoreboards: done");
    }

    private void clearAllCaches() {
        loadedGroups.clear();
        prefixByGroup.clear();
        suffixByGroup.clear();
        tabFormatByGroup.clear();
        chatFormatByGroup.clear();
        sortIdByGroup.clear();
        nameColorByGroup.clear();
        plugin.getDebugLog().debug("GroupManager.clearAllCaches: caches cleared");
    }

    private String buildTeamKey(String rawGroupName) {
        String paddedSortId = sortIdByGroup.getOrDefault(rawGroupName, "9999");
        String key = paddedSortId + Encoder.key(rawGroupName);
        key = key.replace(' ', '_');
        if (key.length() > TEAM_NAME_MAX_LENGTH) key = key.substring(0, TEAM_NAME_MAX_LENGTH);
        return key;
    }

}