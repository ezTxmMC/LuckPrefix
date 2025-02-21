package de.eztxm.luckprefix.database.sql;

import lombok.Getter;

@Getter
public enum Update {

    UPDATE_GROUP("update `luckprefix_groups` set `%s`='%s' where `group`='%s'");

    private final String query;

    Update(String query) {
        this.query = query;
    }
}
