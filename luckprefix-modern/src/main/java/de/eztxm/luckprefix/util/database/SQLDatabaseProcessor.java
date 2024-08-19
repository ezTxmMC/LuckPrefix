package de.eztxm.luckprefix.util.database;

import de.eztxm.api.database.SQLConnection;
import de.eztxm.luckprefix.database.sql.Table;

public class SQLDatabaseProcessor {
    private final SQLConnection connection;

    public SQLDatabaseProcessor(SQLConnection connection) {
        this.connection = connection;
    }

    public void createTable(Table table) {
        this.connection.put(table.getQuery());
    }
}
