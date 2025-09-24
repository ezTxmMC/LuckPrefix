package de.eztxm.luckprefix.common.config;

import java.nio.file.Path;
import java.util.List;

public final class DatabaseConfig extends AbstractConfig {
    private final String defaultSqlitePath;

    public DatabaseConfig(Path filePath, String defaultSqlitePath) {
        super(filePath);
        this.defaultSqlitePath = defaultSqlitePath;
    }

    @Override
    protected void defineDefaults() {
        addDefault("Database.Enabled", false);
        addDefault("Database.Type", "SQLITE");

        addDefault("Database.SQLite.Path", defaultSqlitePath);
        addDefault("Database.SQLite.FileName", "sqlite.db");

        addDefault("Database.MariaDB.Host", "localhost");
        addDefault("Database.MariaDB.Port", 3306);
        addDefault("Database.MariaDB.Database", "luckprefix");
        addDefault("Database.MariaDB.User", "luckprefix");
        addDefault("Database.MariaDB.Password", "");

        addDefault("Database.MongoDB.Host", "localhost");
        addDefault("Database.MongoDB.Port", 27017);
        addDefault("Database.MongoDB.Database", "luckprefix");
        addDefault("Database.MongoDB.User", "luckprefix");
        addDefault("Database.MongoDB.Password", "");

        saveDefaults();
        setComments("Database", List.of(
                "Supported types: SQLITE and MARIADB; disable to use file-based storage"
        ));
        saveComments();
    }

    public boolean isDatabaseEnabled() {
        return getBoolean("Database.Enabled", false);
    }

    public void setDatabaseEnabled(boolean enabled) {
        set("Database.Enabled", enabled);
    }

    public String getDatabaseType() {
        return getString("Database.Type", "SQLITE");
    }

    public void setDatabaseType(String type) {
        set("Database.Type", type);
    }

    public String getSqlitePath() {
        return getString("Database.SQLite.Path", defaultSqlitePath);
    }

    public void setSqlitePath(String path) {
        set("Database.SQLite.Path", path);
    }

    public String getSqliteFileName() {
        return getString("Database.SQLite.FileName", "sqlite.db");
    }

    public void setSqliteFileName(String fileName) {
        set("Database.SQLite.FileName", fileName);
    }

    public String getMariaDbHost() {
        return getString("Database.MariaDB.Host", "localhost");
    }

    public void setMariaDbHost(String host) {
        set("Database.MariaDB.Host", host);
    }

    public int getMariaDbPort() {
        return getInt("Database.MariaDB.Port", 3306);
    }

    public void setMariaDbPort(int port) {
        set("Database.MariaDB.Port", port);
    }

    public String getMariaDbDatabase() {
        return getString("Database.MariaDB.Database", "luckprefix");
    }

    public void setMariaDbDatabase(String database) {
        set("Database.MariaDB.Database", database);
    }

    public String getMariaDbUser() {
        return getString("Database.MariaDB.User", "luckprefix");
    }

    public void setMariaDbUser(String user) {
        set("Database.MariaDB.User", user);
    }

    public String getMariaDbPassword() {
        return getString("Database.MariaDB.Password", "");
    }

    public void setMariaDbPassword(String password) {
        set("Database.MariaDB.Password", password);
    }

    public String getMongoHost() {
        return getString("Database.MongoDB.Host", "localhost");
    }

    public void setMongoHost(String host) {
        set("Database.MongoDB.Host", host);
    }

    public int getMongoPort() {
        return getInt("Database.MongoDB.Port", 27017);
    }

    public void setMongoPort(int port) {
        set("Database.MongoDB.Port", port);
    }

    public String getMongoDatabase() {
        return getString("Database.MongoDB.Database", "luckprefix");
    }

    public void setMongoDatabase(String database) {
        set("Database.MongoDB.Database", database);
    }

    public String getMongoUser() {
        return getString("Database.MongoDB.User", "luckprefix");
    }

    public void setMongoUser(String user) {
        set("Database.MongoDB.User", user);
    }

    public String getMongoPassword() {
        return getString("Database.MongoDB.Password", "");
    }

    public void setMongoPassword(String password) {
        set("Database.MongoDB.Password", password);
    }
}