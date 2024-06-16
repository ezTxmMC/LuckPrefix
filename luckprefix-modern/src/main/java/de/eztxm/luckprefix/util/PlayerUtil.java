package de.eztxm.luckprefix.util;

import lombok.Getter;
import net.luckperms.api.model.group.Group;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerUtil {
    private final Map<UUID, BukkitTask> joinSchedulers;
    private final Map<UUID, Group> userGroups;

    public PlayerUtil() {
        this.joinSchedulers = new HashMap<>();
        this.userGroups = new HashMap<>();
    }

    public void addJoinScheduler(UUID uuid, BukkitTask bukkitTask) {
        this.joinSchedulers.put(uuid, bukkitTask);
    }

    public void addUserGroup(UUID uuid, Group group) {
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
