package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SortIdSubCommand {

    public static void execute(Player player, Audience adventurePlayer, Group group, String[] args, FileConfiguration groupsConfig, ConfigManager groupsFile) {
        if (args.length == 5) {
            try {
                int sortID = Integer.parseInt(args[4]);
                groupsConfig.set(group.getName().toLowerCase() + ".SortID", sortID);
                groupsFile.reloadConfig();
                sortID = groupsConfig.getInt(group.getName().toLowerCase() + ".SortID");
                adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix()
                        + "The sort-id of the group <#33ffff>" + group.getName() + " <gray>is now: " + sortID).miniMessage());
                return;
            } catch (NumberFormatException e) {
                adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This isn't a number.").miniMessage());
            }
        }
        String sortID = groupsConfig.getString(group.getName().toLowerCase() + ".SortID");
        adventurePlayer.sendMessage(
                MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix()
                        + "The sort-id of the group <#33ffff>" + group.getName() + " <gray>is: " + sortID));
    }
}
