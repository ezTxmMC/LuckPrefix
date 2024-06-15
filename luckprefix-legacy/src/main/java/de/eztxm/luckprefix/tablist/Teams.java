package de.eztxm.luckprefix.tablist;

import de.eztxm.luckprefix.LuckPrefix;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
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
                if (!playerIntegerHashMap.containsKey(player)) playerIntegerHashMap.put(player, 0);
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
        if (sortID.length() == 1) {
            if (scoreboard.getTeam("000" + sortID + group.getName()) != null) return;
            scoreboard.registerNewTeam("000" + sortID + group.getName());
            scoreboard.getTeam("000" + sortID + group.getName()).setPrefix(LuckPrefix.getInstance().getConfig().getString("Groups." + group.getName() + ".TabFormat").replace('&', '§').replace("%>>", "»"));
        }
        if (sortID.length() == 2) {
            if (scoreboard.getTeam("00" + sortID + group.getName()) != null) return;
            scoreboard.registerNewTeam("00" + sortID + group.getName());
            scoreboard.getTeam("00" + sortID + group.getName()).setPrefix(LuckPrefix.getInstance().getConfig().getString("Groups." + group.getName() + ".TabFormat").replace('&', '§').replace("%>>", "»"));
        }
        if (sortID.length() == 3) {
            if (scoreboard.getTeam("0" + sortID + group.getName()) != null) return;
            scoreboard.registerNewTeam("0" + sortID + group.getName());
            scoreboard.getTeam("0" + sortID + group.getName()).setPrefix(LuckPrefix.getInstance().getConfig().getString("Groups." + group.getName() + ".TabFormat").replace('&', '§').replace("%>>", "»"));
        }
        if (sortID.length() >= 4) {
            Bukkit.getConsoleSender().sendMessage("§eLuckPrefix §8| §cSortID for " + group.getName() + " to high");
        }
    }
}
