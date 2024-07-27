package de.eztxm.luckprefix.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class TextUtil {
    private final Object input;

    public TextUtil(Object input) {
        this.input = input;
    }

    public Component miniMessage() {
        return MiniMessage.miniMessage().deserialize((String) input);
    }

    public String legacyMiniMessage() {
        return LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize((String) input));
    }

    public String legacy() {
        return LegacyComponentSerializer.legacySection().serialize((Component) input);
    }
}
