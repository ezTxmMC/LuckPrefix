package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.DatabaseConfig;
import de.eztxm.luckprefix.common.config.GroupsConfig;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

public class TabformatSubCommand {

    public static void execute(Audience adventurePlayer, Group group, String[] args, ConfigService configService) {
        GroupsConfig  groupsConfig = configService.of(GroupsConfig.class);
        if (args.length > 4) {
            String value = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
            DatabaseConfig databaseConfig = configService.of(DatabaseConfig.class);
            if (databaseConfig.isDatabaseEnabled()) {
                // TODO: Database integration
                return;
            }
            groupsConfig.setTabFormat(group.getName(), value);
            groupsConfig.save();
            LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
            String tabformat = groupsConfig.getTabFormat(group.getName());
            adventurePlayer.sendMessage(new Text("The tabformat of the group <#33ffff>" + group.getName() + " <gray>is now: " + tabformat).prefixMiniMessage());
            return;
        }
        String tabformat = groupsConfig.getTabFormat(group.getName());
        adventurePlayer.sendMessage(new Text("The tabformat of the group <#33ffff>" + group.getName() + " <gray>is: " + tabformat).prefixMiniMessage());
    }
}
