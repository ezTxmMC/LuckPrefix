package de.eztxm.luckprefix.command;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.command.subcommand.GroupSubCommand;
import de.eztxm.luckprefix.command.subcommand.ReloadConfigsSubCommand;
import de.eztxm.luckprefix.util.Text;
import net.kyori.adventure.audience.Audience;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LuckPrefixCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            LuckPrefix.getInstance().getLogger().warning("You must be a player to use this command.");
            return false;
        }
        Audience adventurePlayer = LuckPrefix.getInstance().getAdventure().player(player);
        if (!player.hasPermission("luckprefix.command")) {
            adventurePlayer.sendMessage(new Text("<#ff3333>You don't have the permission to use this command.").prefixMiniMessage());
            return false;
        }
        if (args.length < 1) {
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
        switch (args[0]) {
            case "group" -> {
                return GroupSubCommand.execute(adventurePlayer, args);
            }
            case "reloadconfig" -> {
                return ReloadConfigsSubCommand.execute(adventurePlayer);
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
                List<String> arguments = new ArrayList<>(List.of("prefix", "suffix", "tabformat", "chatformat", "sortID", "namecolor"));
                arguments.removeIf(argument -> !argument.startsWith(args[2]));
                return arguments;
            }
        }
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("prefix")) {
                    List<String> arguments = new ArrayList<>(List.of("set", "clear"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("suffix")) {
                    List<String> arguments = new ArrayList<>(List.of("set", "clear"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("tabformat")) {
                    List<String> arguments = new ArrayList<>(List.of("set"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("chatformat")) {
                    List<String> arguments = new ArrayList<>(List.of("set"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("sortID")) {
                    List<String> arguments = new ArrayList<>(List.of("set"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("namecolor")) {
                    List<String> arguments = new ArrayList<>(List.of("set"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
            }
        }
        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("prefix")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.removeIf(argument -> !argument.startsWith(args[4]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("suffix")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.removeIf(argument -> !argument.startsWith(args[4]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("tabformat")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.removeIf(argument -> !argument.startsWith(args[4]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("chatformat")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.removeIf(argument -> !argument.startsWith(args[4]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("sortID")) {
                    List<String> arguments = new ArrayList<>(List.of("<number>"));
                    arguments.removeIf(argument -> !argument.startsWith(args[4]));
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("namecolor")) {
                    List<String> colors = new ArrayList<>();
                    for (ChatColor color : ChatColor.values()) {
                        if (color.isColor() && !color.isFormat()) {
                            colors.add(color.name().toUpperCase());
                        }
                    }
                    colors.removeIf(argument -> !argument.startsWith(args[4]));
                    return colors;
                }
            }
        }
        return Collections.emptyList();
    }
}
