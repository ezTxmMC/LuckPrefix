package de.eztxm.luckprefix.command.subcommand;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.api.manager.IGroupManager;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.MainConfig;
import de.eztxm.luckprefix.util.Text;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;

public class ReloadCommand {

    @SneakyThrows
    public static boolean execute(Audience adventurePlayer) {
        adventurePlayer.sendMessage(new Text("Reloading configurations...").prefixMiniMessage());
        ConfigService configService = LuckPrefix.getInstance().getConfigService();
        configService.reloadAll();
        IGroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
        groupManager.reloadFromConfig();
        LuckPrefix.getInstance().startConfigWatcher(configService.of(MainConfig.class));
        adventurePlayer.sendMessage(new Text("Reloaded configurations.").prefixMiniMessage());
        return true;
    }
}
