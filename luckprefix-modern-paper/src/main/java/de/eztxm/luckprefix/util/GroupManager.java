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
        boolean autoAdd = this.instance.getConfig().getBoolean("Auto-Add-Group", true);
        boolean warnMissing = this.instance.getConfig().getBoolean("Warning-If-Group-Can-Not-Loaded", true);

        if (!config.isConfigurationSection(group)) {
            if (!autoAdd) {
                if (warnMissing) this.instance.getLogger().warning("Gruppe '" + group + "' existiert nicht in groups.yml – überspringe Laden.");
                return;
            }
            config.createSection(group);
            if (warnMissing) this.instance.getLogger().warning("groups.yml: Gruppe '" + group + "' fehlte – lege sie mit Defaults an.");
        }

        setIfBlank(config, group + ".Prefix", "<gray>" + group, "Prefix", group, warnMissing);
        setIfBlank(config, group + ".Suffix", "", "Suffix", group, warnMissing);
        setIfBlank(config, group + ".Tabformat", "<prefix> <dark_gray>| <gray><player>", "Tabformat", group, warnMissing);
        setIfBlank(config, group + ".Chatformat", "<prefix> <dark_gray>- <gray><player><dark_gray> » <gray><message>",
                "Chatformat", group, warnMissing);
        if (!config.isSet(group + ".SortID")) {
            config.set(group + ".SortID", 90);
            if (warnMissing) this.instance.getLogger().warning("groups.yml: '" + group + ".SortID' fehlte – setze Default 90.");
        }
        setIfBlank(config, group + ".NameColor", "gray", "NameColor", group, warnMissing);

        String prefix = safeGetString(config, group + ".Prefix", "");
        String suffix = safeGetString(config, group + ".Suffix", "");
        String tabFmt = safeGetString(config, group + ".Tabformat", "<prefix> <dark_gray>| <gray><player>");
        String chatFmt = safeGetString(config, group + ".Chatformat",
                "<prefix> <dark_gray>- <gray><player><dark_gray> » <gray><message>");
        int sortId = config.getInt(group + ".SortID", 90);

        String sortIdPadded = String.format("%04d", Math.max(0, sortId));

        NamedTextColor color = NamedTextColor.GRAY;
        String colorStr = safeGetString(config, group + ".NameColor", "gray");
        try {
            color = Text.fromString(colorStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            this.instance.getLogger().warning("groups.yml: '" + group + ".NameColor' = '" + colorStr + "' ist ungültig – setze GRAY.");
        }

        this.groups.add(group);
        this.groupPrefix.put(group, prefix);
        this.groupSuffix.put(group, suffix);
        this.groupTabformat.put(group, tabFmt);
        this.groupChatformat.put(group, chatFmt);
        this.groupID.put(group, sortIdPadded);
        this.groupColor.put(group, color);

        try { this.instance.getGroupsFile().saveConfiguration(); } catch (Exception ignored) {}
    }

    private void setIfBlank(FileConfiguration config, String path, Object defVal, String keyName, String group, boolean warn) {
        String cur = config.isSet(path) ? String.valueOf(config.get(path)) : null;
        boolean missing = cur == null;
        boolean blank = false;
        if (!missing) blank = cur.trim().isEmpty();
        if (missing || blank) {
            config.set(path, defVal);
            if (warn) this.instance.getLogger().warning("groups.yml: '" + group + "." + keyName +
                    (missing ? "' fehlte" : "' war leer") + " – setze Default: " + defVal);
        }
    }

    private String safeGetString(FileConfiguration config, String path, String defVal) {
        String string = config.getString(path);
        if (string == null) return defVal;
        return string;
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
