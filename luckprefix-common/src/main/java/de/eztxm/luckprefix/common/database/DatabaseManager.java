package de.eztxm.luckprefix.common.database;

import de.eztxm.luckprefix.common.database.util.DatabaseType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {

    private static DatabaseManager instance;
    private final Map<String, DatabaseConnection> connections = new ConcurrentHashMap<>();
    private DatabaseType defaultType = DatabaseType.SQLITE;



}
