package de.eztxm.luckprefix.database.sql;

import lombok.Getter;

@Getter
public enum Table {

    CREATE_GROUPS_TABLE("CREATE TABLE IF NOT EXISTS `luckprefix_groups`(`group` VARCHAR(64), `prefix` VARCHAR(32), `suffix`VARCHAR(32), `tabformat` VARCHAR(64), `chatformat` VARCHAR(64), `sortId` INT, `namecolor` VARCHAR(10));"),
    CREATE_MESSAGES_TABLE("CREATE TABLE IF NOT EXISTS `luckprefix_messages`(`type` VARCHAR(32), `message` VARCHAR(255));");

    private final String query;

    Table(String query) {
        this.query = query;
    }
}
