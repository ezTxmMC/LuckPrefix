package de.eztxm.luckprefix.command.subcommand;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.MainConfig;
import de.eztxm.luckprefix.group.GroupManager;
import de.eztxm.luckprefix.util.Text;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;

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
