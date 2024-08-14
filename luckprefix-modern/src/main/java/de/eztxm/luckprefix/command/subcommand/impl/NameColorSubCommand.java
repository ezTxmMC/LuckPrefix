package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class NameColorSubCommand {

    public static boolean execute(Audience adventurePlayer, Group group, String[] args, FileConfiguration groupsConfig, ConfigManager groupsFile) {
        if (args.length == 5) {
            try {
                try {
                ChatColor color = ChatColor.valueOf(args[4].toUpperCase());
                if (!color.isColor() || color.isFormat()) {
                    adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This isn't a color.").miniMessage());
                    return false;
                }
                groupsConfig.set(group.getName().toLowerCase() + ".NameColor", color.name().toLowerCase());
                groupsFile.reloadConfig();
                LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
                String nameColor = groupsConfig.getString(group.getName().toLowerCase() + ".NameColor");
                adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix()
                        + "The name-color of the group <#33ffff>" + group.getName() + " <gray>is now: " + nameColor).miniMessage());
                return true;
                } catch (IllegalArgumentException e) {
                    adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This isn't a valid color option.").miniMessage());
                }
            } catch (EnumConstantNotPresentException e) {
                adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This isn't a number.").miniMessage());
            }
        }
        String nameColor = groupsConfig.getString(group.getName().toLowerCase() + ".NameColor");
        adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix()
                + "The name-color of the group <#33ffff>" + group.getName() + " <gray>is: " + nameColor).miniMessage());
        return true;
    }
}
