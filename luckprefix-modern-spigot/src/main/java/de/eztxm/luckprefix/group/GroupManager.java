package de.eztxm.luckprefix.group;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.GroupsConfig;
import de.eztxm.luckprefix.common.config.MainConfig;
import de.eztxm.luckprefix.common.logging.DebugLog;
import de.eztxm.luckprefix.common.util.Encoder;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                Team team = scoreboard.getTeam(buildTeamKey(removed.getRawName(), removed.getSortId()));
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

                String teamName = buildTeamKey(meta.getRawName(), meta.getSortId());
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
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> setupGroups(viewer));
            return;
        }
        if (viewer == null) return;

        Scoreboard scoreboard = viewer.getScoreboard();
        for (String groupName : new ArrayList<>(loadedGroups)) {
            GroupMeta meta = metaByGroup.get(groupName);
            if (meta == null) continue;

            String teamName = buildTeamKey(meta.getRawName(), meta.getSortId());
            Team team = ensureTeam(scoreboard, teamName);

            applyTeamDecor(team, meta, viewer);
        }
    }

    private boolean checkGroup(String rawGroupName, GroupsConfig groups) {
        if (!groups.hasGroup(rawGroupName)) {
            groups.setPrefix(rawGroupName, "");
            groups.setSuffix(rawGroupName, "");
            groups.setTabFormat(rawGroupName, "<prefix><player>");
            groups.setChatFormat(rawGroupName, "<prefix><player>: <message>");
            groups.setSortId(rawGroupName, nextSortId());
            groups.setNameColor(rawGroupName, "white");
            return true;
        }
        boolean changed = false;

        if(groups.getPrefix(rawGroupName).isEmpty()) {
            groups.setPrefix(rawGroupName, "");
            changed = true;
        }
        if (groups.getSuffix(rawGroupName) == null) {
            groups.setSuffix(rawGroupName, "");
            changed = true;
        }
        if (groups.getTabFormat(rawGroupName) == null) {
            groups.setTabFormat(rawGroupName, "<prefix><player>");
            changed = true;
        }
        if (groups.getChatFormat(rawGroupName) == null) {
            groups.setChatFormat(rawGroupName, "<prefix><player>: <message>");
            changed = true;
        }
        int sort = groups.getSortId(rawGroupName);
        if (sort <= 0) {
            groups.setSortId(rawGroupName, nextSortId()); changed = true;
        }
        if (groups.getNameColor(rawGroupName) == null) {
            groups.setNameColor(rawGroupName, "white"); changed = true;
        }

        return changed;
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
        GroupsConfig groups = cfg.of(GroupsConfig.class);

        String prefix = groups.getPrefix(rawGroupName);
        String suffix = groups.getSuffix(rawGroupName);
        String tab = groups.getTabFormat(rawGroupName);
        String chat = groups.getChatFormat(rawGroupName);

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

        String tab = meta.getTabFormat();

        if (tab != null && tab.contains("<prefix>") && !meta.getPrefix().isEmpty()) {
            try {
                Component c = new Text(tab
                        .replace("<prefix>", meta.getPrefix())
                        .replace("<suffix>", "")
                        .replace("<player>", "")
                ).placeholders(viewer).miniMessage();
                team.setPrefix(LegacyComponentSerializer.legacySection().serialize(c));
            } catch (Exception ex) {
                plugin.getDebugLog().error("applyTeamDecor: prefix build failed for group " + meta.getRawName(), ex);
            }
        }

        if (tab != null && tab.contains("<suffix>") && !meta.getSuffix().isEmpty()) {
            try {
                Component c = new Text(meta.getSuffix()).placeholders(viewer).miniMessage();
                team.setSuffix(LegacyComponentSerializer.legacySection().serialize(c));
            } catch (Exception ex) {
                plugin.getDebugLog().error("applyTeamDecor: suffix build failed for group " + meta.getRawName(), ex);
            }
        }

        if (meta.getNameColor() != null) {
            try { team.setColor(convertNamedTextColorToChatColor(meta.getNameColor())); }
            catch (Exception ex) {
                plugin.getDebugLog().error("applyTeamDecor: team.color failed for group " + meta.getRawName(), ex);
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
        return metaByGroup.values().stream().mapToInt(GroupMeta::getSortId).max().orElse(0) + 10;
    }

    public String getPrefixByGroup(String group) {
        return metaByGroup.get(group).getPrefix();
    }

    public String getSuffixByGroup(String group) {
        return metaByGroup.get(group).getSuffix();
    }

    public int getSortIdByGroup(String group) {
        return metaByGroup.get(group).getSortId();
    }

    public String getSortIdAsStringByGroup(String group) {
        return Integer.toString(metaByGroup.get(group).getSortId());
    }

    public String getTabFormatByGroup(String group) {
        return metaByGroup.get(group).getTabFormat();
    }

    public String getChatFormatByGroup(String group) {
        return metaByGroup.get(group).getChatFormat();
    }

    public NamedTextColor getNameColorByGroup(String group) {
        return metaByGroup.get(group).getNameColor();
    }

    public List<String> getLoadedGroups() {
        return List.copyOf(loadedGroups);
    }

    public ChatColor convertNamedTextColorToChatColor(NamedTextColor color) {
        if (color.equals(NamedTextColor.BLACK)) return ChatColor.BLACK;
        if (color.equals(NamedTextColor.DARK_BLUE)) return ChatColor.DARK_BLUE;
        if (color.equals(NamedTextColor.DARK_GREEN)) return ChatColor.DARK_GREEN;
        if (color.equals(NamedTextColor.DARK_AQUA)) return ChatColor.DARK_AQUA;
        if (color.equals(NamedTextColor.DARK_RED)) return ChatColor.DARK_RED;
        if (color.equals(NamedTextColor.DARK_PURPLE)) return ChatColor.DARK_PURPLE;
        if (color.equals(NamedTextColor.GOLD)) return ChatColor.GOLD;
        if (color.equals(NamedTextColor.GRAY)) return ChatColor.GRAY;
        if (color.equals(NamedTextColor.DARK_GRAY)) return ChatColor.DARK_GRAY;
        if (color.equals(NamedTextColor.BLUE)) return ChatColor.BLUE;
        if (color.equals(NamedTextColor.GREEN)) return ChatColor.GREEN;
        if (color.equals(NamedTextColor.AQUA)) return ChatColor.AQUA;
        if (color.equals(NamedTextColor.RED)) return ChatColor.RED;
        if (color.equals(NamedTextColor.LIGHT_PURPLE)) return ChatColor.LIGHT_PURPLE;
        if (color.equals(NamedTextColor.YELLOW)) return ChatColor.YELLOW;
        if (color.equals(NamedTextColor.WHITE)) return ChatColor.WHITE;

        return ChatColor.WHITE;
    }
}
