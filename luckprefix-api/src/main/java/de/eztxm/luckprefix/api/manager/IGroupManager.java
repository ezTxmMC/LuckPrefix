package de.eztxm.luckprefix.api.manager;

import de.eztxm.luckprefix.api.unified.ILuckPlayer;
import de.eztxm.luckprefix.api.unified.ILuckScoreboard;

import java.util.List;

public interface IGroupManager {

    void loadGroups();
    void reloadFromConfig();
    void reloadGroup(String rawGroupName);
    void createGroup(String rawGroupName);
    void deleteGroup(String rawGroupName);
    void setGroups(ILuckPlayer player, ILuckScoreboard scoreboard);
    void setupGroups(ILuckPlayer player);
    List<String> getLoadedGroups();

}
