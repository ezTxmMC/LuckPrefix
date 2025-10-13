package de.eztxm.luckprefix.api.unified;

import java.util.Set;

public interface ILuckScoreboard {

    void registerNewObjective(String name);
    void registerNewTeam(String name);
    void unregisterObjective(String name);
    void unregisterTeam(String name);
    void addPlayerToTeam(String playerName, String team);
    void removePlayerFromTeam(String playerName, String team);
    Set<Object> getTeams();
    Set<Object> getObjectives();
    boolean hasObjective(String name);
    boolean hasTeam(String name);

}
