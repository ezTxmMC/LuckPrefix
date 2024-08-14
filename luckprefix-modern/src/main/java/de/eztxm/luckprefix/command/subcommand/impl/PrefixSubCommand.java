package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PrefixSubCommand {

    public static void execute(Player player, Audience adventurePlayer, Group group, String[] args, FileConfiguration groupsConfig, ConfigManager groupsFile) {
        if (args.length > 4) {
            StringBuilder builder = new StringBuilder(args[4]);
            for (int i = 5; i < args.length - 1; i++) {
                builder.append(" ").append(args[i]);
            }
            groupsConfig.set(group.getName().toLowerCase() + ".Prefix", builder.toString());
            groupsFile.reloadConfig();
            String prefix = groupsConfig.getString(group.getName().toLowerCase() + ".Prefix");
            adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix()
                    + "The prefix of the group <#33ffff>" + group.getName() + " <gray>is now: " + prefix).miniMessage());
            return;
        }
        String prefix = groupsConfig.getString(group.getName().toLowerCase() + ".Prefix");
        adventurePlayer.sendMessage(new Text(LuckPrefix.getInstance().getPrefix()
                + "The prefix of the group <#33ffff>" + group.getName() + " <gray>is: " + prefix).miniMessage());
    }
}
