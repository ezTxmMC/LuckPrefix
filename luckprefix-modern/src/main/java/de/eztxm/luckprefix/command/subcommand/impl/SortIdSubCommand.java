package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;
import org.bukkit.configuration.file.FileConfiguration;

public class SortIdSubCommand {

    public static void execute(Audience adventurePlayer, Group group, String[] args, FileConfiguration groupsConfig, ConfigManager groupsFile) {
        if (args.length == 5) {
            try {
                int sortID = Integer.parseInt(args[4]);
                groupsConfig.set(group.getName().toLowerCase() + ".SortID", sortID);
                groupsFile.reloadConfig();
                LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
                sortID = groupsConfig.getInt(group.getName().toLowerCase() + ".SortID");
                adventurePlayer.sendMessage(new Text("The sort-id of the group <#33ffff>" + group.getName() + " <gray>is now: " + sortID).prefixMiniMessage());
                return;
            } catch (NumberFormatException e) {
                adventurePlayer.sendMessage(new Text("<#ff3333>This isn't a number.").prefixMiniMessage());
            }
        }
        String sortID = groupsConfig.getString(group.getName().toLowerCase() + ".SortID");
        adventurePlayer.sendMessage(new Text("The sort-id of the group <#33ffff>" + group.getName() + " <gray>is: " + sortID).prefixMiniMessage());
    }
}
