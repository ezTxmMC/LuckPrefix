package de.eztxm.luckprefix.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.eztxm.luckprefix.LuckPrefix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Text {
    private final String input;

    public Text(String input) {
        this.input = input;
    }

    public Component prefixMiniMessage() {
        return this.miniMessage(LuckPrefix.getInstance().getPrefix() + this.input);
    }

    public Component miniMessage(TagResolver... tagResolvers) {
        return MiniMessage.miniMessage().deserialize(this.input, tagResolvers);
    }

    public Component miniMessage(String input, TagResolver... tagResolvers) {
        return MiniMessage.miniMessage().deserialize(input, tagResolvers);
    }

    public String legacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static NamedTextColor fromString(String colorName) {
        if (colorName == null)
            return null;
        try {
            return NamedTextColor.NAMES.value(colorName.toLowerCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static Component parseLegacy(Component component) {
        return processComponent(component, null);
    }

    private static Component processComponent(Component comp, TextColor currentColor) {
        ComponentBuilder<?, ?> builder = comp.toBuilder();
        TextColor color = (comp.color() != null) ? comp.color() : currentColor;
        if (comp instanceof TextComponent) {
            String text = ((TextComponent) comp).content();
            builder = parseText(text, color).toBuilder();
        }
        builder.children(comp.children().stream()
                .map(child -> processComponent(child, color))
                .toList());
        return builder.build();
    }

    private static Component parseText(String text, TextColor baseColor) {
        Map<Character, NamedTextColor> colorMap = Map.of(
                '0', NamedTextColor.BLACK,
                '1', NamedTextColor.DARK_BLUE,
                '2', NamedTextColor.DARK_GREEN,
                '3', NamedTextColor.DARK_AQUA,
                '4', NamedTextColor.DARK_RED,
                '5', NamedTextColor.DARK_PURPLE,
                '6', NamedTextColor.GOLD,
                '7', NamedTextColor.GRAY,
                '8', NamedTextColor.DARK_GRAY,
                '9', NamedTextColor.BLUE,
                'a', NamedTextColor.GREEN,
                'b', NamedTextColor.AQUA,
                'c', NamedTextColor.RED,
                'd', NamedTextColor.LIGHT_PURPLE,
                'e', NamedTextColor.YELLOW,
                'f', NamedTextColor.WHITE);
        Pattern pattern = Pattern.compile("(&[0-9a-fk-or])|(&#[a-f0-9]{6})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        TextComponent.Builder result = Component.text();
        int lastIndex = 0;
        TextColor currentColor = baseColor;
        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                result.append(Component.text(
                        text.substring(lastIndex, matcher.start()),
                        currentColor));
            }
            String code = matcher.group().substring(1);
            if (code.startsWith("#")) {
                currentColor = TextColor.fromHexString(code);
            } else {
                currentColor = colorMap.getOrDefault(
                        Character.toLowerCase(code.charAt(0)),
                        currentColor);
            }
            lastIndex = matcher.end();
        }
        if (lastIndex < text.length()) {
            result.append(Component.text(
                    text.substring(lastIndex),
                    currentColor));
        }
        return result.build();
    }

}
