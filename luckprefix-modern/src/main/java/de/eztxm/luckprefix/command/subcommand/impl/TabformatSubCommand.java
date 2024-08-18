package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;
import org.bukkit.configuration.file.FileConfiguration;

public class TabformatSubCommand {

    public static void execute(Audience adventurePlayer, Group group, String[] args, FileConfiguration groupsConfig, ConfigManager groupsFile) {
        if (args.length > 4) {
            StringBuilder builder = new StringBuilder(args[4]);
            for (int i = 5; i < args.length; i++) {
                builder.append(" ").append(args[i]);
            }
            groupsConfig.set(group.getName().toLowerCase() + ".Tabformat", builder.toString());
            groupsFile.reloadConfig();
            LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
            String tabformat = groupsConfig.getString(group.getName().toLowerCase() + ".Tabformat");
            adventurePlayer.sendMessage(new Text("The tabformat of the group <#33ffff>" + group.getName() + " <gray>is now: " + tabformat).prefixMiniMessage());
            return;
        }
        String tabformat = groupsConfig.getString(group.getName().toLowerCase() + ".Tabformat");
        adventurePlayer.sendMessage(new Text("The tabformat of the group <#33ffff>" + group.getName() + " <gray>is: " + tabformat).prefixMiniMessage());
    }
}
