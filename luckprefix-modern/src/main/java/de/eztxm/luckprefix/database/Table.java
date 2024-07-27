package de.eztxm.luckprefix.database;

public class Table {

    public static final String CREATE_GROUPS_TABLE = "CREATE TABLE IF NOT EXISTS `lp_groups`(" +
            "`prefix` VARCHAR(32), `suffix`VARCHAR(32), `tabformat` VARCHAR(64), `chatformat` VARCHAR(64), `sortId` INT, `namecolor` VARCHAR(10));";

    public static final String CREATE_MESSAGES_TABLE = "CREATE TABLE IF NOT EXISTS `lp_messages`(`type` VARCHAR(32), `message` VARCHAR(255));";

}
