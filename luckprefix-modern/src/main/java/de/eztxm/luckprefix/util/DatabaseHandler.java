package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.database.Processor;

public class DatabaseHandler {

    public static Processor selectProcessor() {
        return switch (LuckPrefix.getInstance().getDatabaseFile().getValue("Database.Type").asString().toUpperCase()) {
            case "MARIADB", "SQLITE" -> LuckPrefix.getInstance().getSqlDatabaseManager().getProcessor();
            case "MONGODB" -> LuckPrefix.getInstance().getMongoDBManager().getProcessor();
            default -> null;
        };
    }
}
