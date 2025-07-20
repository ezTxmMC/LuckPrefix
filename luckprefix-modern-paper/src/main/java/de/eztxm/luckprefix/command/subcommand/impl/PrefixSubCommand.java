package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.util.database.Processor;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.DatabaseHandler;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PrefixSubCommand {

    public static void execute(Player player, Group group, String[] args, FileConfiguration groupsConfig, ConfigManager groupsFile) {
        if (args.length > 4) {
            StringBuilder builder = new StringBuilder(args[4]);
            for (int i = 5; i < args.length - 1; i++) {
                builder.append(" ").append(args[i]);
            }
            if (LuckPrefix.getInstance().getDatabaseFile().getValue("Database.Enabled").asBoolean()) {
                // TODO: Database integration
                return;
            }
            groupsConfig.set(group.getName().toLowerCase() + ".Prefix", builder.toString());
            groupsFile.reloadConfig();
            LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
            String prefix = groupsConfig.getString(group.getName().toLowerCase() + ".Prefix");
            player.sendMessage(new Text("The prefix of the group <#33ffff>" + group.getName() + " <gray>is now: " + prefix).prefixMiniMessage());
            return;
        }
        if (args.length == 4 && args[3].equalsIgnoreCase("clear")) {
            groupsConfig.set(group.getName().toLowerCase() + ".Prefix", "");
            groupsFile.reloadConfig();
            LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
            player.sendMessage(new Text("The prefix of the group <#33ffff>" + group.getName() + " <gray>has been cleared.").prefixMiniMessage());
            return;
        }
        String prefix = groupsConfig.getString(group.getName().toLowerCase() + ".Prefix");
        player.sendMessage(new Text("The prefix of the group <#33ffff>" + group.getName() + " <gray>is: " + prefix).prefixMiniMessage());
    }
}
