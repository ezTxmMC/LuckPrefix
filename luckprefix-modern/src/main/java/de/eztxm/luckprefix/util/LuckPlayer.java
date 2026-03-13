package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.api.unified.ILuckPlayer;
import de.eztxm.luckprefix.api.unified.ILuckScoreboard;
import org.bukkit.entity.Player;

import java.util.UUID;

public record LuckPlayer(Player player) implements ILuckPlayer {

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public UUID getUniqueId() {
        return this.player.getUniqueId();
    }

    @Override
    public ILuckScoreboard getScoreboard() {
        return new LuckScoreboard(this.player.getScoreboard());
    }
}
