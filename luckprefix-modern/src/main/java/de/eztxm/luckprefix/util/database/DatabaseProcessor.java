package de.eztxm.luckprefix.util.database;

import de.eztxm.api.database.SQLConnection;
import de.eztxm.luckprefix.database.Table;

public class DatabaseProcessor {
    private final SQLConnection connection;

    public DatabaseProcessor(SQLConnection connection) {
        this.connection = connection;
    }

    public void createTable(Table table) {
        this.connection.put(table.getQuery());
    }
}
