package de.eztxm.luckprefix.api.unified;

import java.util.UUID;

public interface ILuckPlayer {

    String getName();
    UUID getUniqueId();
    ILuckScoreboard getScoreboard();
    void setName(String name);
    void setUniqueId(UUID uniqueId);
    void setScoreboard(ILuckScoreboard scoreboard);

}
