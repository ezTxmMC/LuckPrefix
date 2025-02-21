package de.eztxm.luckprefix.command.subcommand;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.GroupManager;
import de.eztxm.luckprefix.util.Text;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReloadConfigsSubCommand {

    @SneakyThrows
    public static boolean execute(Audience adventurePlayer) {
        adventurePlayer.sendMessage(new Text("Reloading configurations...").prefixMiniMessage());
        LuckPrefix.getInstance().getConfig().load(new File("plugins/LuckPrefix/config.yml"));
        LuckPrefix.getInstance().getDatabaseFile().reloadConfig();
        LuckPrefix.getInstance().getGroupsFile().reloadConfig();
        GroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.getScoreboard().getTeams().forEach(Team::unregister);
            if (!groupManager.getGroups().isEmpty()) {
                List<String> groups = new ArrayList<>(groupManager.getGroups());
                for (String group : groups) {
                    groupManager.deleteGroup(group);
                }
            }
            onlinePlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            groupManager.setupGroups(onlinePlayer);
        }
        adventurePlayer.sendMessage(new Text("Reloaded configurations.").prefixMiniMessage());
        return true;
    }
}
