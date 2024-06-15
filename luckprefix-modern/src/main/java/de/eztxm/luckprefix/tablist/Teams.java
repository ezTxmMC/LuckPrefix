package de.eztxm.luckprefix.tablist;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.Gradient;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;

public class Teams {
    private static final HashMap<Player, Integer> playerIntegerHashMap = new HashMap<>();

    public static void setTeam(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        LuckPerms luckPermsApi = LuckPermsProvider.get();
        Bukkit.getOnlinePlayers().forEach(players -> {
            User user = luckPermsApi.getUserManager().getUser(players.getUniqueId());
            String playerGroup = luckPermsApi.getGroupManager().getGroup(user.getPrimaryGroup()).getName();
            String sortID = LuckPrefix.getInstance().getConfig().getString("Groups." + playerGroup + ".SortID");
            if (sortID == null) {
                if (!playerIntegerHashMap.containsKey(players)) playerIntegerHashMap.put(player, 0);
                if (playerIntegerHashMap.get(player) > 0) return;
                if (playerIntegerHashMap.get(player) == 0) {
                    LuckPrefix.getInstance().getLogger().warning("SortId for '" + playerGroup + "' can't loaded. Please check your config!");
                    playerIntegerHashMap.put(player, 1);
                }
                return;
            }
            if (sortID.length() == 1) {
                if (scoreboard.getTeam("000" + sortID + playerGroup) == null) {
                    Bukkit.broadcastMessage("§4§lRestart the Server");
                    return;
                }
                scoreboard.getTeam("000" + sortID + playerGroup).addEntry(players.getName());
                return;
            }
            if (sortID.length() == 2) {
                if (scoreboard.getTeam("00" + sortID + playerGroup) == null) {
                    Bukkit.broadcastMessage("§4§lRestart the Server");
                    return;
                }
                scoreboard.getTeam("00" + sortID + playerGroup).addEntry(players.getName());
                return;
            }
            if (sortID.length() == 3) {
                if (scoreboard.getTeam("0" + sortID + playerGroup) == null) {
                    Bukkit.broadcastMessage("§4§lRestart the Server");
                    return;
                }
                scoreboard.getTeam("0" + sortID + playerGroup).addEntry(players.getName());
                return;
            }
            LuckPrefix.getInstance().getLogger().warning("SortID for '" + playerGroup + "' to high");
        });
    }

    public static void createTeam(Scoreboard scoreboard, Group group) {
        String sortID = LuckPrefix.getInstance().getConfig().getString("Groups." + group.getName() + ".SortID");
        if (sortID == null) {
            LuckPrefix.getInstance().getLogger().warning("SortId for '" + group.getName() + "' can't loaded. Please check your config!");
            return;
        }
        ChatColor color = ChatColor.valueOf(LuckPrefix.getInstance().getConfig().getString("Groups." + group.getName() + ".Color").toUpperCase());
        if (sortID.length() == 1) {
            if (scoreboard.getTeam("000" + sortID + group.getName()) != null) return;
            String tabFormat = LuckPrefix.getInstance().getConfig().getString("Groups." + group.getName() + ".TabFormat");
            scoreboard.registerNewTeam("000" + sortID + group.getName());
            if (gradientEnabled(scoreboard, group, sortID, color, tabFormat)) return;
            scoreboard.getTeam("000" + sortID + group.getName()).setPrefix(tabFormat.replace('&', '§').replace("%>>", "»"));
            scoreboard.getTeam("000" + sortID + group.getName()).setColor(color);
            return;
        }
        if (sortID.length() == 2) {
            if (scoreboard.getTeam("00" + sortID + group.getName()) != null) return;
            String tabFormat = LuckPrefix.getInstance().getConfig().getString("Groups." + group.getName() + ".TabFormat");
            if (gradientEnabled(scoreboard, group, sortID, color, tabFormat)) return;
            scoreboard.registerNewTeam("00" + sortID + group.getName());
            scoreboard.getTeam("00" + sortID + group.getName()).setPrefix(tabFormat.replace('&', '§').replace("%>>", "»"));
            scoreboard.getTeam("00" + sortID + group.getName()).setColor(color);
            return;
        }
        if (sortID.length() == 3) {
            if (scoreboard.getTeam("0" + sortID + group.getName()) != null) return;
            String tabFormat = LuckPrefix.getInstance().getConfig().getString("Groups." + group.getName() + ".TabFormat");
            if (gradientEnabled(scoreboard, group, sortID, color, tabFormat)) return;
            scoreboard.registerNewTeam("0" + sortID + group.getName());
            scoreboard.getTeam("0" + sortID + group.getName()).setPrefix(tabFormat.replace('&', '§').replace("%>>", "»"));
            scoreboard.getTeam("0" + sortID + group.getName()).setColor(color);
            return;
        }
        if (sortID.length() >= 4) {
            Bukkit.getConsoleSender().sendMessage("§eLuckPrefix §8| §cSortID for " + group.getName() + " to high");
        }
    }

    private static boolean gradientEnabled(Scoreboard scoreboard, Group group, String sortID, ChatColor color, String tabFormat) {
        if (LuckPrefix.getInstance().getConfig().getBoolean("Groups." + group.getName() + ".Gradient.Enable")) {
            Gradient gradient = new Gradient(
                    LuckPrefix.getInstance().getConfig().getString("Groups." + group.getName() + ".Gradient.Color1"),
                    LuckPrefix.getInstance().getConfig().getString("Groups." + group.getName() + ".Gradient.Color2")
            );
            scoreboard.getTeam("000" + sortID + group.getName()).setPrefix(gradient.getGradientString(tabFormat.replace('&', '§').replace("%>>", "»")));
            scoreboard.getTeam("000" + sortID + group.getName()).setColor(color);
            return true;
        }
        return false;
    }
}
