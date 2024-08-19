package de.eztxm.luckprefix.util.database;

import de.eztxm.database.MongoDBConnection;

public class MongoDBProcessor {
    private final MongoDBConnection connection;

    public MongoDBProcessor(MongoDBConnection connection) {
        this.connection = connection;
    }
}
