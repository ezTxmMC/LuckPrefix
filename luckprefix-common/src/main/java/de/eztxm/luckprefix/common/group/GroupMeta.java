package de.eztxm.luckprefix.common.group;

import net.kyori.adventure.text.format.NamedTextColor;

public record GroupMeta(String rawName, String prefix, String suffix, String tabFormat, String chatFormat, int sortId,
                        NamedTextColor nameColor) {
    public GroupMeta(String rawName, String prefix, String suffix, String tabFormat, String chatFormat, int sortId, NamedTextColor nameColor) {
        if (rawName == null) {
            throw new IllegalArgumentException("rawName cannot be null");
        }
        this.rawName = rawName;
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix == null ? "" : suffix;
        this.tabFormat = tabFormat == null ? "" : tabFormat;
        this.chatFormat = chatFormat == null ? "" : chatFormat;
        this.sortId = Math.max(0, sortId);
        this.nameColor = nameColor;
    }
}
