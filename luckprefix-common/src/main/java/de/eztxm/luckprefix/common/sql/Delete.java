package de.eztxm.luckprefix.common.sql;

import lombok.Getter;

@Getter
public enum Delete {

    DELETE_GROUP("DELETE FROM `luckprefix_groups` WHERE `group`='%s'");

    private final String query;

    Delete(String query) {
        this.query = query;
    }
}
