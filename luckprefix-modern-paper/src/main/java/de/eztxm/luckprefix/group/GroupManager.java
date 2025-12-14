package de.eztxm.luckprefix.group;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.GroupsConfig;
import de.eztxm.luckprefix.common.config.MainConfig;
import de.eztxm.luckprefix.common.logging.DebugLog;
import de.eztxm.luckprefix.common.metadata.LuckPermsDataHandler;
import de.eztxm.luckprefix.common.util.Encoder;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class GroupManager {

    private static final int TEAM_NAME_MAX_LENGTH = 16;

    private final LuckPrefix plugin;
    private final DebugLog debugLog;

    private final Map<String, GroupMeta> metaByGroup = new ConcurrentHashMap<>();
    private final Set<String> loadedGroups = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile String defaultGroupName = "default";

    public GroupManager(LuckPrefix plugin) {
        if(plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        this.plugin = plugin;
        this.debugLog = plugin.getDebugLog();
    }

    public void loadGroups() {
        debugLog.info("GroupManager.loadGroups: start");

        clearAllCaches();
        collectAndLoadFromConfigs();

        for(Player player : Bukkit.getOnlinePlayers()) {
            try {
                var user = plugin.getLuckPerms().getUserManager().getUser(player.getUniqueId());
                if(user != null) {
                    plugin.getPlayerManager().setPlayerListName(player.getUniqueId(), user.getPrimaryGroup());
                }
            } catch (Exception exception) {
                debugLog.error("loadGroups: setPlayerListName failed for " + player.getName());
            }
        }

        refreshAllScoreboards();
        debugLog.info("GroupManager.loadGroups: done");
    }

    public void reloadAllFromConfigs() {
        debugLog.info("GroupManager.reloadAllFromConfigs: start");
        clearAllCaches();
        collectAndLoadFromConfigs();
        refreshAllScoreboards();
        debugLog.info("GroupManager.reloadAllFromConfigs: done");
    }

    public void reloadGroup(String rawGroupName) {
        if(rawGroupName == null || rawGroupName.isEmpty()) return;
        debugLog.info("GroupManager.reloadGroup: " + rawGroupName);

        loadSingleGroupFromConfigs(rawGroupName);

        refreshAllScoreboards();
    }

    public void createGroup(String rawGroupName) {
        if(rawGroupName == null || rawGroupName.isEmpty()) return;
        if(!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> createGroup(rawGroupName));
            return;
        }

        try {
            ConfigService cfg = plugin.getConfigService();
            GroupsConfig groups = cfg.of(GroupsConfig.class);
            MainConfig main = cfg.of(MainConfig.class);
            groups.ensureGroupDefaults(rawGroupName, main.isAutoAddGroupEnabled(), main.isPrintWarningsEnabled(), debugLog::warn);
            groups.save();
        } catch (Exception exception) {
            debugLog.warn("createGroup: couldn't ensure defaults for " + rawGroupName + ":" + exception.getMessage());
        }

        loadSingleGroupFromConfigs(rawGroupName);

        for(Player viewer : Bukkit.getOnlinePlayers()) {
            setupGroups(viewer);
            setGroups(viewer, viewer.getScoreboard());
        }

        debugLog.info("GroupManager.createGroup: created '" + rawGroupName + "'");
    }

    public void deleteGroup(String rawGroupName) {
        if (rawGroupName == null || rawGroupName.isBlank()) return;
        if(!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> deleteGroup(rawGroupName));
            return;
        }

        GroupMeta removed = metaByGroup.remove(rawGroupName);
        loadedGroups.remove(rawGroupName);

        String enc = Encoder.key(rawGroupName).replace(' ', '_');

        for(Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = viewer.getScoreboard();

            if(removed != null) {
                Team team = scoreboard.getTeam(buildTeamKey(removed.rawName(), removed.sortId()));
                if(team != null) {
                    team.unregister();
                }
            }

            for(Team team : new ArrayList<>(scoreboard.getTeams())) {
                String name = team.getName();
                if(!name.isEmpty() && (name.endsWith(enc) || name.contains(enc))) {
                    team.unregister();
                }
            }
        }

        try {
            var lpGroup = plugin.getLuckPerms().getGroupManager().getGroup(rawGroupName);
            if(lpGroup == null) {
                GroupsConfig groups = plugin.getConfigService().of(GroupsConfig.class);
                if(groups.hasGroup(rawGroupName)) {
                    groups.removeGroup(rawGroupName);
                }
            }
        } catch (Exception exception) {
            debugLog.warn("deleteGroup: couldn't purge config for '" + rawGroupName + "': " + exception.getMessage());
        }

        refreshAllScoreboards();
        debugLog.info("GroupManager.deleteGroup: deleted '" + rawGroupName + "'");
    }

    public void setGroups(Player viewer, Scoreboard scoreboard) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> setGroups(viewer, scoreboard));
            return;
        }
        if (viewer == null || scoreboard == null) return;

        setupGroups(viewer);

        for (Player target : Bukkit.getOnlinePlayers()) {
            try {
                String groupName = resolveGroupName(target.getUniqueId());
                GroupMeta meta = metaByGroup.getOrDefault(groupName, metaByGroup.get(defaultGroupName));
                if (meta == null) continue;

                String teamName = buildTeamKey(meta.rawName(), meta.sortId());
                Team desired = ensureTeam(scoreboard, teamName);

                Team current = scoreboard.getEntryTeam(target.getName());
                boolean already = current != null && current.getName().equals(desired.getName());
                if (already) continue;

                if (current != null) {
                    try { current.removeEntry(target.getName()); }
                    catch (IllegalStateException ex) {
                        plugin.getDebugLog().warn("setGroups: removeEntry failed '" + target.getName() + "' from '" + current.getName() + "': " + ex.getMessage());
                    }
                }
                try { desired.addEntry(target.getName()); }
                catch (IllegalStateException ex) {
                    plugin.getDebugLog().warn("setGroups: addEntry failed '" + target.getName() + "' to '" + desired.getName() + "': " + ex.getMessage());
                }
            } catch (Exception ex) {
                plugin.getDebugLog().error("setGroups: failed for viewer " + viewer.getName(), ex);
            }
        }
    }

    public void setupGroups(Player viewer) {
        MainConfig config = LuckPrefix.getInstance().getConfigService().of(MainConfig.class);
        if (!config.isTabFormattingEnabled()) {
            return;
        }
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> setupGroups(viewer));
            return;
        }
        if (viewer == null) return;
        Scoreboard scoreboard = viewer.getScoreboard();
        for (String groupName : new ArrayList<>(loadedGroups)) {
            GroupMeta meta = metaByGroup.get(groupName);
            if (meta == null) continue;
            String teamName = buildTeamKey(meta.rawName(), meta.sortId());
            Team team = ensureTeam(scoreboard, teamName);
            applyTeamDecor(team, meta, viewer);
        }
    }

    private void refreshAllScoreboards() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::refreshAllScoreboards);
            return;
        }
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
    }

    private void clearAllCaches() {
        loadedGroups.clear();
        metaByGroup.clear();
        plugin.getDebugLog().debug("GroupManager.clearAllCaches: caches cleared");
    }

    private void collectAndLoadFromConfigs() {
        ConfigService cfg = plugin.getConfigService();
        MainConfig main = cfg.of(MainConfig.class);
        GroupsConfig groups = cfg.of(GroupsConfig.class);

        Collection<Group> lpGroups = plugin.getLuckPerms().getGroupManager().getLoadedGroups();
        for (Group lpGroup : lpGroups) {
            String raw = lpGroup.getName();
            if (groups.hasGroup(raw)) loadSingleGroupFromConfigs(raw);
            else if (main.isWarnIfGroupCannotBeLoaded()) {
                plugin.getLogger().warning("Group '" + raw + "' can't be loaded (not found in groups.yml).");
            }
        }
    }

    private void loadSingleGroupFromConfigs(String rawGroupName) {
        ConfigService cfg = plugin.getConfigService();
        MainConfig main = cfg.of(MainConfig.class);
        GroupsConfig groups = cfg.of(GroupsConfig.class);
        LuckPerms luckPerms = this.plugin.getLuckPerms();
        LuckPermsDataHandler luckPermsDataHandler = this.plugin.getLuckPermsDataHandler();

        String prefix = main.isUseLuckPermsMetaDataEnabled() ? luckPermsDataHandler.getPrefix(luckPerms.getGroupManager().getGroup(rawGroupName)) : groups.getPrefix(rawGroupName); // TODO: MOVE TO GROUPSCONFIG WITH API IMPLEMENTED
        String suffix = main.isUseLuckPermsMetaDataEnabled() ? luckPermsDataHandler.getSuffix(luckPerms.getGroupManager().getGroup(rawGroupName)) : groups.getSuffix(rawGroupName); // TODO: MOVE TO GROUPSCONFIG WITH API IMPLEMENTED
        String tab = groups.getTabFormat(rawGroupName);
        String chat = groups.getChatFormat(rawGroupName);

        plugin.getDebugLog().debug("Group -> %s - Prefix -> '%s'".formatted(rawGroupName, prefix));
        plugin.getDebugLog().debug("Group -> %s - Suffix -> '%s'".formatted(rawGroupName, suffix));
        plugin.getDebugLog().debug("Group -> %s - Tab -> '%s'".formatted(rawGroupName, tab));
        plugin.getDebugLog().debug("Group -> %s - Chat -> '%s'".formatted(rawGroupName, chat));

        int sortId = 9999;
        try {
            String id = Integer.toString(groups.getSortId(rawGroupName));
            if (id != null && !id.isBlank()) sortId = Integer.parseInt(id.trim());
        } catch (Exception ex) {
            plugin.getLogger().warning("groups.yml: '" + rawGroupName + ".SortId' invalid – using 9999");
        }

        NamedTextColor color = null;
        try {
            String colorName = groups.getNameColor(rawGroupName);
            if (colorName != null && !colorName.isBlank()) color = NamedTextColor.NAMES.value(colorName.trim().toLowerCase());
        } catch (Exception ex) {
            plugin.getLogger().warning("groups.yml: '" + rawGroupName + ".NameColor' invalid – ignoring");
        }

        metaByGroup.put(rawGroupName, new GroupMeta(rawGroupName, prefix, suffix, tab, chat, sortId, color));
        loadedGroups.add(rawGroupName);
        plugin.getDebugLog().info("GroupManager.createGroup: loaded '" + rawGroupName + "'");
    }

    private String resolveGroupName(UUID playerId) {

        String fromCache = plugin.getPlayerManager().getUserGroups().get(playerId);
        if (fromCache != null && !fromCache.isBlank()) return fromCache;

        var user = plugin.getLuckPerms().getUserManager().getUser(playerId);
        if (user != null && user.getPrimaryGroup() != null && !user.getPrimaryGroup().isBlank()) return user.getPrimaryGroup();

        return defaultGroupName;
    }

    private Team ensureTeam(Scoreboard scoreboard, String teamName) {
        Team team = scoreboard.getTeam(teamName);
        if (team != null) return team;
        try { return scoreboard.registerNewTeam(teamName); }
        catch (IllegalArgumentException ex) {
            String fallback = (teamName + "_" + System.nanoTime());
            if (fallback.length() > TEAM_NAME_MAX_LENGTH) fallback = fallback.substring(0, TEAM_NAME_MAX_LENGTH);
            return scoreboard.getTeam(fallback) != null ? scoreboard.getTeam(fallback) : scoreboard.registerNewTeam(fallback);
        }
    }

    private void applyTeamDecor(Team team, GroupMeta meta, Player viewer) {

        String tab = meta.tabFormat();

        if (tab != null && tab.contains("<prefix>") && !meta.prefix().isEmpty()) {
            try {
                Component c = new Text(tab
                        .replace("<prefix>", meta.prefix())
                        .replace("<suffix>", "")
                        .replace("<player>", "")
                ).placeholders(viewer).miniMessage();
                team.prefix(c);
            } catch (Exception ex) {
                plugin.getDebugLog().error("applyTeamDecor: prefix build failed for group " + meta.rawName(), ex);
            }
        }

        if (tab != null && tab.contains("<suffix>") && !meta.suffix().isEmpty()) {
            try {
                Component c = new Text(meta.suffix()).placeholders(viewer).miniMessage();
                team.suffix(c);
            } catch (Exception ex) {
                plugin.getDebugLog().error("applyTeamDecor: suffix build failed for group " + meta.rawName(), ex);
            }
        }

        if (meta.nameColor() != null) {
            try { team.color(meta.nameColor()); }
            catch (Exception ex) {
                plugin.getDebugLog().error("applyTeamDecor: team.color failed for group " + meta.rawName(), ex);
            }
        }
    }

    private String buildTeamKey(String rawGroupName, int sortId) {

        String padded = String.format("%04d", Math.max(0, sortId));
        String key = padded + Encoder.key(rawGroupName);
        key = key.replace(' ', '_');
        if (key.length() > TEAM_NAME_MAX_LENGTH) key = key.substring(0, TEAM_NAME_MAX_LENGTH);
        return key;
    }

    private int nextSortId() {
        return metaByGroup.values().stream().mapToInt(GroupMeta::sortId).max().orElse(0) + 10;
    }

    public String getPrefixByGroup(String group) {
        return metaByGroup.get(group).prefix();
    }

    public String getSuffixByGroup(String group) {
        return metaByGroup.get(group).suffix();
    }

    public int getSortIdByGroup(String group) {
        return metaByGroup.get(group).sortId();
    }

    public String getSortIdAsStringByGroup(String group) {
        return Integer.toString(metaByGroup.get(group).sortId());
    }

    public String getTabFormatByGroup(String group) {
        return metaByGroup.get(group).tabFormat();
    }

    public String getChatFormatByGroup(String group) {
        return metaByGroup.get(group).chatFormat();
    }

    public NamedTextColor getNameColorByGroup(String group) {
        return metaByGroup.get(group).nameColor();
    }

    public List<String> getLoadedGroups() {
        return List.copyOf(loadedGroups);
    }
}
