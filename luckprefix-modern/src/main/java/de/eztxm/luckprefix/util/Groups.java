package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Groups {
    private final List<Group> groups;
    private final Map<Group, String> groupPrefix;
    private final Map<Group, String> groupSuffix;
    private final Map<Group, String> groupTabformat;
    private final Map<Group, String> groupChatformat;
    private final Map<Group, Integer> groupID;
    private final Map<Group, ChatColor> groupColor;
    private final Map<Group, Boolean> groupGradient;
    private final Map<Group, String> groupGradientColorOne;
    private final Map<Group, String> groupGradientColorTwo;

    public Groups() {
        groups = new ArrayList<>();
        groupPrefix = new HashMap<>();
        groupSuffix = new HashMap<>();
        groupTabformat = new HashMap<>();
        groupChatformat = new HashMap<>();
        groupID = new HashMap<>();
        groupColor = new HashMap<>();
        groupGradient = new HashMap<>();
        groupGradientColorOne = new HashMap<>();
        groupGradientColorTwo = new HashMap<>();
    }

    public void setGroup(Group group, Player player) {

    }

    public void createGroup(Group group) {
        FileConfiguration config = LuckPrefix.getInstance().getConfig();
        if (config.get("Groups." + group.getName()) == null) return;
        this.groups.add(group);
        this.groupPrefix.put(group, config.getString("Groups." + group.getName() + ".Prefix"));
        this.groupSuffix.put(group, config.getString("Groups." + group.getName() + ".Suffix"));
        this.groupTabformat.put(group, config.getString("Groups." + group.getName() + ".Tabformat"));
        this.groupChatformat.put(group, config.getString("Groups." + group.getName() + ".Chatformat"));
        this.groupID.put(group, config.getInt("Groups." + group.getName() + ".SortID"));
        this.groupColor.put(group, ChatColor.valueOf(config.getString("Groups." + group.getName() + ".Color")));
        if (config.get("Groups." + group.getName() + ".Gradient") == null) return;
        if (config.get("Groups." + group.getName() + ".Gradient.Enabled") == null) return;
        this.groupGradient.put(group, config.getBoolean("Groups." + group.getName() + ".Gradient.Enabled"));
        if (!config.getBoolean("Groups." + group.getName() + ".Gradient.Enabled")) return;
        this.groupGradientColorOne.put(group, config.getString("Groups." + group.getName() + ".Gradient.Color1"));
        this.groupGradientColorTwo.put(group, config.getString("Groups." + group.getName() + ".Gradient.Color2"));
    }

    public void deleteGroup(String groupName) {
        LuckPerms luckPerms = LuckPrefix.getInstance().getLuckPerms();
        Group group = luckPerms.getGroupManager().getGroup(groupName);
        this.groups.remove(group);
        this.groupPrefix.remove(group);
        this.groupSuffix.remove(group);
        this.groupTabformat.remove(group);
        this.groupChatformat.remove(group);
        this.groupID.remove(group);
        this.groupColor.remove(group);
        this.groupGradient.remove(group);
        this.groupGradientColorOne.remove(group);
        this.groupGradientColorTwo.remove(group);
    }

    private boolean gradientEnabled(Scoreboard scoreboard, Group group, String sortID, ChatColor color, String tabFormat) {

        return false;
    }
}
