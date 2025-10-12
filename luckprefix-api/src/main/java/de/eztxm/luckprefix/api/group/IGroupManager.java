package de.eztxm.luckprefix.api.group;

import de.eztxm.luckprefix.api.unified.ILuckPlayer;
import de.eztxm.luckprefix.api.unified.ILuckScoreboard;

public interface IGroupManager {

    void loadGroups();
    void reloadFromConfig();
    void reloadGroup(String rawGroupName);
    void createGroup(String rawGroupName);
    void deleteGroup(String rawGroupName);
    void setGroups(ILuckPlayer player, ILuckScoreboard scoreboard);

}
