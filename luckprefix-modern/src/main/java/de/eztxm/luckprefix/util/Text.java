package de.eztxm.luckprefix.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Text {
    private final Object input;

    public Text(Object input) {
        this.input = input;
    }

    public Component miniMessage() {
        return MiniMessage.miniMessage().deserialize((String) input);
    }

    public String legacyMiniMessage(TagResolver... tagResolvers) {
        return LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize((String) input, tagResolvers));
    }
}
