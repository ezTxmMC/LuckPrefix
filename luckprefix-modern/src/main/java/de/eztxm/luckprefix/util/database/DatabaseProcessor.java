package de.eztxm.luckprefix.util.database;

import de.eztxm.api.database.SQLConnection;

public class DatabaseProcessor {
    private final SQLConnection connection;

    public DatabaseProcessor(SQLConnection connection) {
        this.connection = connection;
    }
}
