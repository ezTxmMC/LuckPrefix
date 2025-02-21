package de.eztxm.luckprefix.database.sql;

import lombok.Getter;

@Getter
public enum Insert {

    INSERT_GROUP("INSERT INTO `luckprefix_groups`(`group`, `prefix`, `suffix`, `tabformat`, `chatformat`, `sortId`, `namecolor`) VALUES ('%s','%s','%s','%s','%s',%s,'%s')");

    private final String query;

    Insert(String query) {
        this.query = query;
    }
}
