package de.eztxm.luckprefix.command;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.ConfigManager;
import de.eztxm.luckprefix.util.GroupManager;
import de.eztxm.luckprefix.util.GroupType;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LuckPrefixCommand implements TabExecutor {

    @SneakyThrows
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            LuckPrefix.getInstance().getLogger().warning("You must be a player to use this command.");
            return false;
        }
        Audience adventurePlayer = LuckPrefix.getInstance().getAdventure().player(player);
        if (!player.hasPermission("luckprefix.command")) {
            adventurePlayer.sendMessage(
                    MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix() + "<#ff3333>You don't have the permission to use this command."));
            return false;
        }
        if (args.length < 1) {
            adventurePlayer.sendMessage(
                    MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This command needs arguments to work."));
            return false;
        }
        switch (args[0]) {
            case "group" -> {

                // /luckprefix group <name> prefix - Shows the current prefix
                // /luckprefix group <name> prefix set <string> - Set the current prefix
                // /luckprefix group <name> suffix - Shows the current suffix
                // /luckprefix group <name> suffix set <string> - Set the current suffix
                // /luckprefix group <name> tabformat - Shows the current tabformat
                // /luckprefix group <name> tabformat set <string> - Set the current tabformat
                // /luckprefix group <name> chatformat - Shows the current chatformat
                // /luckprefix group <name> chatformat set <string> - Set the current chatformat
                // /luckprefix group <name> sortid - Shows the current sortid
                // /luckprefix group <name> sortid set <string> - Set the current sortid
                // /luckprefix group <name> namecolor - Shows the current namecolor
                // /luckprefix group <name> namecolor set <string> - Set the current namecolor

                ConfigManager groupsFile = LuckPrefix.getInstance().getGroupsFile();
                FileConfiguration groupsConfig = LuckPrefix.getInstance().getGroupsFile().getConfiguration();
                LuckPerms luckPerms = LuckPrefix.getInstance().getLuckPerms();
                Group group = luckPerms.getGroupManager().getGroup(args[1]);
                if (args.length < 3) {
                    adventurePlayer.sendMessage(
                            MiniMessage.miniMessage().deserialize("""
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
                                    <dark_gray><st>------------</st><#77ef77>LuckPrefix<dark_gray><st>------------</st>"""));
                    return false;
                }
                if (group == null) {
                    adventurePlayer.sendMessage(
                            MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This group doesn't exist."));
                    return false;
                }
                try {
                    GroupType groupType = GroupType.valueOf(args[2].toUpperCase());
                    switch (groupType) {
                        case PREFIX -> {
                            if (args.length > 4) {
                                StringBuilder builder = new StringBuilder(args[4]);
                                for (int i = 5; i < args.length - 1; i++) {
                                    builder.append(" ").append(args[i]);
                                }
                                groupsConfig.set(group.getName().toLowerCase() + ".Prefix", builder.toString());
                                groupsFile.saveConfiguration();
                                return true;
                            }
                            String prefix = groupsConfig.getString(group.getName().toLowerCase() + ".Prefix");
                            adventurePlayer.sendMessage(
                                    MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix()
                                            + "The prefix of the group <#33ffff>" + group.getName() + " <gray>is: " + prefix));
                            return true;
                        }
                        case SUFFIX -> {
                            if (args.length > 4) {
                                StringBuilder builder = new StringBuilder(args[4]);
                                for (int i = 5; i < args.length - 1; i++) {
                                    builder.append(" ").append(args[i]);
                                }
                                groupsConfig.set(group.getName().toLowerCase() + ".Suffix", builder.toString());
                                groupsFile.saveConfiguration();
                                return true;
                            }
                            String prefix = groupsConfig.getString(group.getName().toLowerCase() + ".Suffix");
                            adventurePlayer.sendMessage(
                                    MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix()
                                            + "The suffix of the group <#33ffff>" + group.getName() + " <gray>is: " + prefix));
                            return true;
                        }
                        case CHATFORMAT -> {
                            if (args.length > 4) {
                                StringBuilder builder = new StringBuilder(args[4]);
                                for (int i = 5; i < args.length - 1; i++) {
                                    builder.append(" ").append(args[i]);
                                }
                                groupsConfig.set(group.getName().toLowerCase() + ".Chatformat", builder.toString());
                                groupsFile.saveConfiguration();
                                return true;
                            }
                            String prefix = groupsConfig.getString(group.getName().toLowerCase() + ".Chatformat");
                            adventurePlayer.sendMessage(
                                    MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix()
                                            + "The chatformat of the group <#33ffff>" + group.getName() + " <gray>is: " + prefix));
                            return true;
                        }
                        case TABFORMAT -> {
                            if (args.length > 4) {
                                StringBuilder builder = new StringBuilder(args[4]);
                                for (int i = 5; i < args.length - 1; i++) {
                                    builder.append(" ").append(args[i]);
                                }
                                groupsConfig.set(group.getName().toLowerCase() + ".Tabformat", builder.toString());
                                groupsFile.saveConfiguration();
                                return true;
                            }
                            String prefix = groupsConfig.getString(group.getName().toLowerCase() + ".Tabformat");
                            adventurePlayer.sendMessage(
                                    MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix()
                                            + "The tabformat of the group <#33ffff>" + group.getName() + " <gray>is: " + prefix));
                            return true;
                        }
                        case SORTID -> {
                            if (args.length == 5) {
                                try {
                                    int sortID = Integer.parseInt(args[4]);
                                    groupsConfig.set(group.getName().toLowerCase() + ".SortID", sortID);
                                    groupsFile.saveConfiguration();
                                    return true;
                                } catch (NumberFormatException e) {
                                    adventurePlayer.sendMessage(
                                            MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This isn't a number."));
                                }
                            }
                            String prefix = groupsConfig.getString(group.getName().toLowerCase() + ".SortID");
                            adventurePlayer.sendMessage(
                                    MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix()
                                            + "The sort-id of the group <#33ffff>" + group.getName() + " <gray>is: " + prefix));
                            return true;
                        }
                        case NAMECOLOR -> {
                            if (args.length == 5) {
                                try {
                                    ChatColor color = ChatColor.valueOf(args[4].toUpperCase());
                                    if (!color.isColor() || color.isFormat()) {
                                        adventurePlayer.sendMessage(
                                                MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This isn't a color."));
                                        return false;
                                    }
                                    groupsConfig.set(group.getName().toLowerCase() + ".NameColor", color.name().toLowerCase());
                                    groupsFile.saveConfiguration();
                                    return true;
                                } catch (EnumConstantNotPresentException e) {
                                    adventurePlayer.sendMessage(
                                            MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This isn't a number."));
                                }
                            }
                            String prefix = groupsConfig.getString(group.getName().toLowerCase() + ".SortID");
                            adventurePlayer.sendMessage(
                                    MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix()
                                            + "The sort-id of the group <#33ffff>" + group.getName() + " <gray>is: " + prefix));
                            return true;
                        }
                    }
                } catch (EnumConstantNotPresentException e) {
                    adventurePlayer.sendMessage(
                            MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix() + "<#ff3333>This group type doesn't exist."));
                }
            }
            case "reloadconfig" -> {
                adventurePlayer.sendMessage(
                        MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix() + "Reloading configurations..."));
                LuckPrefix.getInstance().getConfig().load(new File("plugins/LuckPrefix/config.yml"));
                LuckPrefix.getInstance().getDatabaseFile().getConfiguration().load(new File("plugins/LuckPrefix/database.yml"));
                LuckPrefix.getInstance().getDatabaseFile().getConfiguration().load(new File("plugins/LuckPrefix/groups.yml"));
                GroupManager groupManager = LuckPrefix.getInstance().getGroupManager();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.getScoreboard().getTeams().forEach(Team::unregister);
                    if (!groupManager.getGroups().isEmpty()) {
                        List<String> groups = new ArrayList<>(groupManager.getGroups());
                        for (String group : groups) {
                            groupManager.deleteGroup(group);
                        }
                    }
                    onlinePlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    groupManager.setupGroups(onlinePlayer);
                }
                adventurePlayer.sendMessage(
                        MiniMessage.miniMessage().deserialize(LuckPrefix.getInstance().getPrefix() + "Reloaded configurations."));
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> arguments = new ArrayList<>(List.of("group", "reloadconfig"));
            arguments.removeIf(argument -> !argument.startsWith(args[0]));
            return arguments;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("group")) {
                LuckPerms luckPerms = LuckPrefix.getInstance().getLuckPerms();
                Set<Group> groups = luckPerms.getGroupManager().getLoadedGroups();
                List<String> arguments = new ArrayList<>();
                groups.forEach(group -> arguments.add(group.getName()));
                arguments.removeIf(argument -> !argument.startsWith(args[1]));
                return arguments;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("group")) {
                List<String> arguments = new ArrayList<>(List.of("prefix", "suffix", "tabformat", "chatformat", "sortID", "color"));
                arguments.removeIf(argument -> !argument.startsWith(args[2]));
                return arguments;
            }
        }
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("prefix")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("suffix")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("tabformat")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("chatformat")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("sortID")) {
                    List<String> arguments = new ArrayList<>(List.of("<number>"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("color")) {
                    List<String> colors = new ArrayList<>();
                    for (ChatColor color : ChatColor.values()) {
                        if (color.isColor() && !color.isFormat()) {
                            colors.add(color.name().toUpperCase());
                        }
                    }
                    colors.removeIf(argument -> !argument.startsWith(args[3]));
                    return colors;
                }
            }
        }
        return Collections.emptyList();
    }
}
