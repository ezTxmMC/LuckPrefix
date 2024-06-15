package de.eztxm.luckprefix.util;

import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class PlayerUtil {
    private final HashMap<UUID, BukkitTask> joinSchedulers;

    public PlayerUtil() {
        this.joinSchedulers = new HashMap<>();
    }

    public void addJoinScheduler(UUID uuid, BukkitTask bukkitTask) {
        this.joinSchedulers.put(uuid, bukkitTask);
    }

    public void cancelJoinScheduler(UUID uuid) {
        this.joinSchedulers.get(uuid).cancel();
    }

    public void removeJoinScheduler(UUID uuid) {
        this.joinSchedulers.remove(uuid);
    }
}
