package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.DatabaseConfig;
import de.eztxm.luckprefix.common.config.GroupsConfig;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;

import java.util.Arrays;

public class PrefixSubCommand {

    public static void execute(Audience adventurePlayer, Group group, String[] args, ConfigService configService) {
        GroupsConfig groupsConfig = configService.of(GroupsConfig.class);
        if (args.length > 4) {
            String value = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
            DatabaseConfig databaseConfig = configService.of(DatabaseConfig.class);
            if (databaseConfig.isDatabaseEnabled()) {
                // TODO: Database integration
                return;
            }
            groupsConfig.setPrefix(group.getName(), value);
            groupsConfig.save();
            LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
            String prefix = groupsConfig.getPrefix(group.getName());
            adventurePlayer.sendMessage(new Text("The prefix of the group <#33ffff>" + group.getName() + " <gray>is now: " + prefix).prefixMiniMessage());
            return;
        }
        if (args.length == 4 && args[3].equalsIgnoreCase("clear")) {
            groupsConfig.setPrefix(group.getName(), "");
            groupsConfig.reload();
            LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
            adventurePlayer.sendMessage(new Text("The prefix of the group <#33ffff>" + group.getName() + " <gray>has been cleared.").prefixMiniMessage());
            return;
        }
        String prefix = groupsConfig.getPrefix(group.getName());
        adventurePlayer.sendMessage(new Text("The prefix of the group <#33ffff>" + group.getName() + " <gray>is: " + prefix).prefixMiniMessage());
    }
}
