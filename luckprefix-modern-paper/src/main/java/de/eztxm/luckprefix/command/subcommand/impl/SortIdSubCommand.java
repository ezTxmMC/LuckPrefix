package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.config.ConfigService;
import de.eztxm.luckprefix.common.config.DatabaseConfig;
import de.eztxm.luckprefix.common.config.GroupsConfig;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;
import org.bukkit.configuration.file.FileConfiguration;

public class SortIdSubCommand {

    public static void execute(Audience adventurePlayer, Group group, String[] args, ConfigService configService) {
        GroupsConfig groupsConfig = configService.of(GroupsConfig.class);
        if (args.length == 5) {
            try {
                int sortID = Integer.parseInt(args[4]);
                DatabaseConfig  databaseConfig = configService.of(DatabaseConfig.class);
                if (databaseConfig.isDatabaseEnabled()) {
                    // TODO: Database integration
                    return;
                }
                groupsConfig.setSortId(group.getName(), sortID);
                groupsConfig.save();
                LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
                sortID = groupsConfig.getSortId(group.getName());
                adventurePlayer.sendMessage(new Text("The sort-id of the group <#33ffff>" + group.getName() + " <gray>is now: " + sortID).prefixMiniMessage());
                return;
            } catch (NumberFormatException e) {
                adventurePlayer.sendMessage(new Text("<#ff3333>This isn't a number.").prefixMiniMessage());
            }
        }
        String sortID = String.valueOf(groupsConfig.getSortId(group.getName()));
        adventurePlayer.sendMessage(new Text("The sort-id of the group <#33ffff>" + group.getName() + " <gray>is: " + sortID).prefixMiniMessage());
    }
}
