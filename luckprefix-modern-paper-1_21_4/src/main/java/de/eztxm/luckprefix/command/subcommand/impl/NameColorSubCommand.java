package de.eztxm.luckprefix.command.subcommand.impl;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.common.util.database.Processor;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.DatabaseHandler;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.model.group.Group;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;

public class NameColorSubCommand {

    public static boolean execute(Audience adventurePlayer, Group group, String[] args, FileConfiguration groupsConfig,
            ConfigManager groupsFile) {
        if (args.length == 5) {
            try {
                try {
                    NamedTextColor color;
                    try {
                        color = NamedTextColor.NAMES.value(args[4].toLowerCase());
                    } catch (IllegalArgumentException ex) {
                        adventurePlayer
                                .sendMessage(new Text("<#ff3333>This isn't a valid color option.").prefixMiniMessage());
                        return false;
                    }
                    if (color == null) {
                        adventurePlayer.sendMessage(new Text("<#ff3333>This isn't a color.").prefixMiniMessage());
                        return false;
                    }
                    if (LuckPrefix.getInstance().getDatabaseFile().getValue("Database.Enabled").asBoolean()) {
                        Processor processor = DatabaseHandler.selectProcessor();
                        if (processor == null) {
                            LuckPrefix.getInstance().getLogger().warning("No database processor selected.");
                            return false;
                        }
                        processor.updateGroup(group.getName().toLowerCase(), "namecolor",
                                NamedTextColor.NAMES.key(color));
                    } else {
                        groupsConfig.set(group.getName().toLowerCase() + ".NameColor", NamedTextColor.NAMES.key(color));
                        groupsFile.reloadConfig();
                    }
                    LuckPrefix.getInstance().getGroupManager().reloadGroup(group.getName());
                    String nameColor = groupsConfig.getString(group.getName().toLowerCase() + ".NameColor");
                    adventurePlayer.sendMessage(new Text(
                            "The name-color of the group <#33ffff>" + group.getName() + " <gray>is now: " + nameColor)
                            .prefixMiniMessage());
                    return true;
                } catch (IllegalArgumentException e) {
                    adventurePlayer
                            .sendMessage(new Text("<#ff3333>This isn't a valid color option.").prefixMiniMessage());
                }
            } catch (EnumConstantNotPresentException e) {
                adventurePlayer.sendMessage(new Text("<#ff3333>This isn't a number.").prefixMiniMessage());
            }
        }
        String nameColor = groupsConfig.getString(group.getName().toLowerCase() + ".NameColor");
        adventurePlayer.sendMessage(
                new Text("The name-color of the group <#33ffff>" + group.getName() + " <gray>is: " + nameColor)
                        .prefixMiniMessage());
        return true;
    }
}
