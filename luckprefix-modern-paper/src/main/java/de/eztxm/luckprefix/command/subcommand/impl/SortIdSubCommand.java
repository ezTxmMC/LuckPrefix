package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.Text;
import net.luckperms.api.model.group.Group;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SortIdSubCommand {

    public static void execute(Player player, Group group, String[] args, FileConfiguration groupsConfig, ConfigManager groupsFile) {
        if (args.length == 5) {
            try {
                int sortID = Integer.parseInt(args[4]);
                if (LuckPrefix.getInstance().getDatabaseFile().getValue("Database.Enabled").asBoolean()) {
                    // TODO: Database integration
                    return;
                }
                groupsConfig.set(group.getName().toLowerCase() + ".SortID", sortID);
                groupsFile.reloadConfig();
                LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
                sortID = groupsConfig.getInt(group.getName().toLowerCase() + ".SortID");
                player.sendMessage(new Text("The sort-id of the group <#33ffff>" + group.getName() + " <gray>is now: " + sortID).prefixMiniMessage());
                return;
            } catch (NumberFormatException e) {
                player.sendMessage(new Text("<#ff3333>This isn't a number.").prefixMiniMessage());
            }
        }
        String sortID = groupsConfig.getString(group.getName().toLowerCase() + ".SortID");
        player.sendMessage(new Text("The sort-id of the group <#33ffff>" + group.getName() + " <gray>is: " + sortID).prefixMiniMessage());
    }
}
