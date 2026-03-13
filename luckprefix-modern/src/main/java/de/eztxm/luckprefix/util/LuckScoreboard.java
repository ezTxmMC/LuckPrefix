package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.api.unified.ILuckScoreboard;
import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collections;
import java.util.Set;

public record LuckScoreboard(Scoreboard scoreboard) implements ILuckScoreboard {

    @Override
    public void registerNewObjective(String name) {
        if (scoreboard.getObjective(name) != null) {
            return;
        }
        scoreboard.registerNewObjective(name, Criteria.DUMMY, Component.empty());
    }

    @Override
    public void registerNewTeam(String name) {
        if (scoreboard.getTeam(name) != null) {
            return;
        }
        scoreboard.registerNewTeam(name);
    }

    @Override
    public void unregisterObjective(String name) {
        if (scoreboard.getObjective(name) == null) {
            return;
        }
        scoreboard.getObjective(name);
    }

    @Override
    public void unregisterTeam(String name) {
        if (scoreboard.getTeam(name) == null) {
            return;
        }
        scoreboard.getTeam(name).unregister();
    }

    @Override
    public void addPlayerToTeam(String playerName, String team) {
        if (scoreboard.getTeam(team) == null) {
            return;
        }
        if (scoreboard.getTeam(team).hasEntry(playerName)) {
            return;
        }
        scoreboard.getTeam(team).addEntry(playerName);
    }

    @Override
    public void removePlayerFromTeam(String playerName, String team) {
        if (scoreboard.getTeam(team) != null) {
            return;
        }
        if (!scoreboard.getTeam(team).hasEntry(playerName)) {
            return;
        }
        scoreboard.getTeam(team).removeEntry(playerName);
    }

    @Override
    public Set<Object> getTeams() {
        return Collections.singleton(scoreboard.getTeams());
    }

    @Override
    public Set<Object> getObjectives() {
        return Collections.singleton(scoreboard.getObjectives());
    }

    @Override
    public boolean hasObjective(String name) {
        return scoreboard.getObjective(name) != null;
    }

    @Override
    public boolean hasTeam(String name) {
        return scoreboard.getTeam(name) != null;
    }
}
