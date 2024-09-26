package de.eztxm.luckprefix.database.sql;

import lombok.Getter;

@Getter
public enum Select {

    SELECT_GROUP("select * from `luckprefix_groups` where `group`='%s'"),
    SELECT_GROUP_VALUE("select `%s` from `luckprefix_groups` where `group`='%s'");

    private final String query;

    Select(String query) {
        this.query = query;
    }
}
