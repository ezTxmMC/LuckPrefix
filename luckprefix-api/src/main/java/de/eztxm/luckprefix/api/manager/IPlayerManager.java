package de.eztxm.luckprefix.api.manager;

import java.util.Map;
import java.util.UUID;

public interface IPlayerManager {

    void initializePlayer(UUID uuid, String groupName);
    void setPlayerListName(UUID uuid, String groupName);
    void setUserGroup(UUID uuid, String groupName);
    void removeUserGroup(UUID uuid);
    Map<UUID, String> getUserGroups();

}
