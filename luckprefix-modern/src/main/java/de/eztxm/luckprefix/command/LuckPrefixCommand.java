package de.eztxm.luckprefix.command;

import de.eztxm.luckprefix.LuckPrefix;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
                LuckPerms luckPerms = LuckPrefix.getInstance().getLuckPerms();
            }
            case "reloadconfig" -> {

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
                List<String> arguments = new ArrayList<>(List.of("prefix", "suffix", "tabformat", "chatformat", "sortID", "color", "gradient", "gradientCourse"));
                arguments.removeIf(argument -> !argument.startsWith(args[2]));
                return arguments;
            }
        }
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("group")) {
                if (args[2].equalsIgnoreCase("prefix")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.forEach(arguments::remove);
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("suffix")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.forEach(arguments::remove);
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("tabformat")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.forEach(arguments::remove);
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("chatformat")) {
                    List<String> arguments = new ArrayList<>(List.of("<text>"));
                    arguments.forEach(arguments::remove);
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("sortID")) {
                    List<String> arguments = new ArrayList<>(List.of("<number>"));
                    arguments.forEach(arguments::remove);
                    return arguments;
                }
                if (args[2].equalsIgnoreCase("color")) {
                    List<String> colors = new ArrayList<>();
                    for (ChatColor color : ChatColor.values()) {
                        if (color.isColor()) {
                            colors.add(color.name().toUpperCase());
                        }
                    }
                    colors.removeIf(argument -> !argument.startsWith(args[3]));
                    return colors;
                }
                if (args[2].equalsIgnoreCase("gradient")) {
                    List<String> arguments = new ArrayList<>(List.of("on", "off"));
                    arguments.removeIf(argument -> !argument.startsWith(args[3]));
                    return arguments;
                }
            }
        }
        return Collections.emptyList();
    }
}
