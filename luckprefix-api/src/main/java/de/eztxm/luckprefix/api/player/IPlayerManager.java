package de.eztxm.luckprefix.api.player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IPlayerManager {

    void reloadScoreboard(UUID uniqueId);

    void reloadAllScoreboards();

    void refreshNametags(UUID uniqueId);

    void refreshAllNametags();

    default CompletableFuture<Void> reloadScoreboardAsync(UUID uniqueId) {
        return CompletableFuture.runAsync(() -> reloadScoreboard(uniqueId));
    }

}
