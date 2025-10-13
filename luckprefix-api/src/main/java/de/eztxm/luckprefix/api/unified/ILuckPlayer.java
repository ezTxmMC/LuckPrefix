package de.eztxm.luckprefix.api.unified;

import java.util.UUID;

public interface ILuckPlayer {

    String getName();
    UUID getUniqueId();
    ILuckScoreboard getScoreboard();

}
