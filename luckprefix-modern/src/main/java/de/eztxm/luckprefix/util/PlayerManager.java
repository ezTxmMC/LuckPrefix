package de.eztxm.luckprefix.util;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerManager {
    private final Map<UUID, BukkitTask> joinSchedulers;
    private final Map<UUID, String> userGroups;

    public PlayerManager() {
        this.joinSchedulers = new HashMap<>();
        this.userGroups = new HashMap<>();
    }

    public void initializePlayer(UUID uuid, String group) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        userGroups.put(uuid, group);
    }

    public void addJoinScheduler(UUID uuid, BukkitTask bukkitTask) {
        this.joinSchedulers.put(uuid, bukkitTask);
    }

    public void addUserGroup(UUID uuid, String group) {
        this.userGroups.put(uuid, group);
    }

    public void cancelJoinScheduler(UUID uuid) {
        this.joinSchedulers.get(uuid).cancel();
    }

    public void removeJoinScheduler(UUID uuid) {
        this.joinSchedulers.remove(uuid);
    }

    public void removeUserGroup(UUID uuid) {
        this.userGroups.remove(uuid);
    }
}
