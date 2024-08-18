package de.eztxm.luckprefix.command.subcommand;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.command.subcommand.impl.*;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.GroupType;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.configuration.file.FileConfiguration;

public class GroupSubCommand {

    public static boolean execute(Audience adventurePlayer, String[] args) {
        if (args.length < 3) {
            adventurePlayer.sendMessage(new Text("""
                                    <dark_gray><st>------------</st><#77ef77>LuckPrefix<dark_gray><st>------------</st>
                                    <dark_gray>» <gray>/luckprefix group <name> prefix - Shows the current prefix
                                    <dark_gray>» <gray>/luckprefix group <name> prefix set <string> - Set the current prefix
                                    <dark_gray>» <gray>/luckprefix group <name> suffix - Shows the current suffix
                                    <dark_gray>» <gray>/luckprefix group <name> suffix set <string> - Set the current suffix
                                    <dark_gray>» <gray>/luckprefix group <name> tabformat - Shows the current tabformat
                                    <dark_gray>» <gray>/luckprefix group <name> tabformat set <string> - Set the current tabformat
                                    <dark_gray>» <gray>/luckprefix group <name> chatformat - Shows the current chatformat
                                    <dark_gray>» <gray>/luckprefix group <name> chatformat set <string> - Set the current chatformat
                                    <dark_gray>» <gray>/luckprefix group <name> sortid - Shows the current sortid
                                    <dark_gray>» <gray>/luckprefix group <name> sortid set <string> - Set the current sortid
                                    <dark_gray>» <gray>/luckprefix group <name> namecolor - Shows the current namecolor
                                    <dark_gray>» <gray>/luckprefix group <name> namecolor set <string> - Set the current namecolor
                                    <dark_gray>» <gray>/luckprefix reloadconfigs - Reloads all configurations
                                    <dark_gray><st>------------</st><#77ef77>LuckPrefix<dark_gray><st>------------</st>""").miniMessage());
            return false;
        }
        ConfigManager groupsFile = LuckPrefix.getInstance().getGroupsFile();
        FileConfiguration groupsConfig = LuckPrefix.getInstance().getGroupsFile().getConfiguration();
        LuckPerms luckPerms = LuckPrefix.getInstance().getLuckPerms();
        Group group = luckPerms.getGroupManager().getGroup(args[1]);
        if (group == null) {
            adventurePlayer.sendMessage(new Text("<#ff3333>This group doesn't exist.").prefixMiniMessage());
            return false;
        }
        try {
            GroupType groupType = GroupType.valueOf(args[2].toUpperCase());
            switch (groupType) {
                case PREFIX -> {
                    PrefixSubCommand.execute(adventurePlayer, group, args, groupsConfig, groupsFile);
                    return true;
                }
                case SUFFIX -> {
                    SuffixSubCommand.execute(adventurePlayer, group, args, groupsConfig, groupsFile);
                    return true;
                }
                case CHATFORMAT -> {
                    ChatformatSubCommand.execute(adventurePlayer, group, args, groupsConfig, groupsFile);
                    return true;
                }
                case TABFORMAT -> {
                    TabformatSubCommand.execute(adventurePlayer, group, args, groupsConfig, groupsFile);
                    return true;
                }
                case SORTID -> {
                    SortIdSubCommand.execute(adventurePlayer, group, args, groupsConfig, groupsFile);
                    return true;
                }
                case NAMECOLOR -> {
                    return NameColorSubCommand.execute(adventurePlayer, group, args, groupsConfig, groupsFile);
                }
                default -> {
                    return false;
                }
            }
        } catch (EnumConstantNotPresentException e) {
            adventurePlayer.sendMessage(new Text("<#ff3333>This group type doesn't exist.").prefixMiniMessage());
            return false;
        }
    }
}
