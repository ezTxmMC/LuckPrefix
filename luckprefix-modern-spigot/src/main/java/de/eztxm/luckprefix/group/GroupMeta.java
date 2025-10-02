package de.eztxm.luckprefix.group;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public final class GroupMeta {
    private final String rawName;
    private final String prefix;
    private final String suffix;
    private final String tabFormat;
    private final String chatFormat;
    private final int sortId;
    private final NamedTextColor nameColor;

    public GroupMeta(String rawName, String prefix, String suffix, String tabFormat, String chatFormat, int sortId, NamedTextColor nameColor) {
        if(rawName == null) {
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
