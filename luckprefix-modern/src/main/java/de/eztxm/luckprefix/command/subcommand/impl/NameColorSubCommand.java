package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class NameColorSubCommand {

    public static boolean execute(Player player, Audience adventurePlayer, Group group, String[] args, FileConfiguration groupsConfig, ConfigManager groupsFile) {
        if (args.length == 5) {
            try {
                ChatColor color = ChatColor.valueOf(args[4].toUpperCase());
                if (!color.isColor() || color.isFormat()) {
                    adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This isn't a color.").miniMessage());
                    return false;
                }
                groupsConfig.set(group.getName().toLowerCase() + ".NameColor", color.name().toLowerCase());
                groupsFile.reloadConfig();
                return true;
            } catch (EnumConstantNotPresentException e) {
                adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This isn't a number.").miniMessage());
            }
        }
        String prefix = groupsConfig.getString(group.getName().toLowerCase() + ".SortID");
        adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix()
                + "The sort-id of the group <#33ffff>" + group.getName() + " <gray>is: " + prefix).miniMessage());
        return true;
    }
}
