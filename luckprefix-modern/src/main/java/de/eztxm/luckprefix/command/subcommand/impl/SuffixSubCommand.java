package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;
import org.bukkit.configuration.file.FileConfiguration;

public class SuffixSubCommand {

    public static void execute(Audience adventurePlayer, Group group, String[] args, FileConfiguration groupsConfig, ConfigManager groupsFile) {
        if (args.length > 4) {
            StringBuilder builder = new StringBuilder(args[4]);
            for (int i = 5; i < args.length - 1; i++) {
                builder.append(" ").append(args[i]);
            }
            groupsConfig.set(group.getName().toLowerCase() + ".Suffix", builder.toString());
            groupsFile.reloadConfig();
            LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
            String suffix = groupsConfig.getString(group.getName().toLowerCase() + ".Suffix");
            adventurePlayer.sendMessage(new Text("The suffix of the group <#33ffff>" + group.getName() + " <gray>is now: " + suffix).prefixMiniMessage());
            return;
        }
        if (args.length == 4 && args[3].equalsIgnoreCase("clear")) {
            groupsConfig.set(group.getName().toLowerCase() + ".Suffix", "");
            groupsFile.reloadConfig();
            LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
            adventurePlayer.sendMessage(new Text("The suffix of the group <#33ffff>" + group.getName() + " <gray>has been cleared.").prefixMiniMessage());
            return;
        }
        String suffix = groupsConfig.getString(group.getName().toLowerCase() + ".Suffix");
        adventurePlayer.sendMessage(new Text("The suffix of the group <#33ffff>" + group.getName() + " <gray>is: " + suffix).prefixMiniMessage());
    }
}
