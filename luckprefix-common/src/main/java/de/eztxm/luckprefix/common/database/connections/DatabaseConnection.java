package de.eztxm.luckprefix.common.database.connections;

import de.eztxm.luckprefix.common.database.util.DatabaseConfig;
import de.eztxm.luckprefix.common.database.util.DatabaseType;

public class DatabaseConnection {
    protected final DatabaseConfig config;

    protected DatabaseConnection(DatabaseConfig config) {
        this.config = config;
    }

    public abstract void connect();
    public abstract void close();
    public abstract boolean isConnected();
    public abstract Object getNativeConnection();
    public abstract DatabaseType getType();

}
