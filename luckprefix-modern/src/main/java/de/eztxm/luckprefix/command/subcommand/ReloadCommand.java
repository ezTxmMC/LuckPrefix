package de.eztxm.luckprefix.command.subcommand;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.api.manager.IGroupManager;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.MainConfig;
import de.eztxm.luckprefix.util.Text;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;

public class ReloadCommand {

    @SneakyThrows
    public static boolean execute(Audience adventurePlayer) {
        adventurePlayer.sendMessage(new Text("Reloading configurations...").prefixMiniMessage());
        LuckPrefix plugin = LuckPrefix.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ConfigService configService = plugin.getConfigService();
            try {
                configService.reloadAll();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    IGroupManager groupManager = plugin.getGroupManager();
                    groupManager.reloadFromConfig();
                    plugin.startConfigWatcher(configService.of(MainConfig.class));
                    adventurePlayer.sendMessage(new Text("Reloaded configurations.").prefixMiniMessage());
                });
            } catch (Exception exception) {
                plugin.getDebugLog().error("Reload command failed", exception);
                Bukkit.getScheduler().runTask(plugin, () ->
                        adventurePlayer.sendMessage(new Text("Reload failed. Check debug.log for details.").prefixMiniMessage()));
            }
        });
        return true;
    }
}
