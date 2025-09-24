package de.eztxm.luckprefix.command.subcommand;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.DatabaseConfig;
import de.eztxm.luckprefix.common.config.GroupsConfig;
import de.eztxm.luckprefix.common.config.MainConfig;
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
        ConfigService configService = LuckPrefix.getInstance().getConfigService();
        configService.reloadAll();
        GroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
        groupManager.reloadAllFromConfigs();
        LuckPrefix.getInstance().startConfigWatcher(configService.of(MainConfig.class));
        adventurePlayer.sendMessage(new Text("Reloaded configurations.").prefixMiniMessage());
        return true;
    }
}
