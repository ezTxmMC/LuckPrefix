package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Text {
    private String input;

    public Text(String input) {
        this.input = input;
    }

    public Text placeholders(Player player) {
        this.input = PlaceholderAPI.setPlaceholders(player, this.input);
        return this;
    }

    public Component prefixMiniMessage() {
        return this.miniMessage(LuckPrefix.getInstance().getPrefix() + this.input);
    }

    public Component miniMessage() {
        return MiniMessage.miniMessage().deserialize(this.input);
    }

    public Component miniMessage(String input) {
        return MiniMessage.miniMessage().deserialize(input);
    }

    public Component component() {
        return Component.text(this.input);
    }

    public String legacyMiniMessage(TagResolver... tagResolvers) {
        return this.alternateColorCodes(this.legacy(MiniMessage.miniMessage().deserialize(this.input, tagResolvers)));
    }

    public String legacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public String alternateColorCodes(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
