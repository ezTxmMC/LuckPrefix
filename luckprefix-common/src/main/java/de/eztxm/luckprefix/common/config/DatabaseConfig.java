package de.eztxm.luckprefix.common.config;

import de.eztxm.luckprefix.api.config.AbstractConfig;
import de.eztxm.luckprefix.api.logging.DebugLog;

import java.nio.file.Path;
import java.util.List;

public final class DatabaseConfig extends AbstractConfig {

    public DatabaseConfig(Path filePath, DebugLog debugLog) {
        super(filePath, debugLog);
        getDebugLog().info("DatabaseConfig: constructed for " + filePath);
    }

    @Override
    protected void defineDefaults() {
        getDebugLog().info("DatabaseConfig.defineDefaults: applying");

        addDefault("Database.Enabled", false);
        addDefault("Database.Type", "SQLITE");

        addDefault("Database.SQLite.Path", path().getParent().resolve("storage").toString().replace("\\", "/"));
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
        getDebugLog().debug("DatabaseConfig.defineDefaults: defaults saved");

        setComments("Database", List.of("Supported: SQLITE, MARIADB, MONGODB (disable via Enabled=false)"));
        saveComments();
        getDebugLog().debug("DatabaseConfig.defineDefaults: comments saved");
    }

    public boolean isDatabaseEnabled() {
        boolean value = getBoolean("Database.Enabled", false);
        getDebugLog().debug("DatabaseConfig.isDatabaseEnabled -> " + value);
        return value;
    }

    public String getDatabaseType() {
        String value = getString("Database.Type", "SQLITE");
        getDebugLog().debug("DatabaseConfig.getDatabaseType -> " + value);
        return value;
    }

    public String getSqlitePath() {
        String value = getString("Database.SQLite.Path", path().getParent().resolve("storage").toString().replace("\\", "/"));
        getDebugLog().debug("DatabaseConfig.getSqlitePath -> " + value);
        return value;
    }

    public String getSqliteFileName() {
        String value = getString("Database.SQLite.FileName", "sqlite.db");
        getDebugLog().debug("DatabaseConfig.getSqliteFileName -> " + value);
        return value;
    }

    public String getMariaHost() {
        String value = getString("Database.MariaDB.Host", "localhost");
        getDebugLog().debug("DatabaseConfig.getMariaHost -> " + value);
        return value;
    }

    public int getMariaPort() {
        int value = getInt("Database.MariaDB.Port", 3306);
        getDebugLog().debug("DatabaseConfig.getMariaPort -> " + value);
        return value;
    }

    public String getMariaDatabase() {
        String value = getString("Database.MariaDB.Database", "luckprefix");
        getDebugLog().debug("DatabaseConfig.getMariaDatabase -> " + value);
        return value;
    }

    public String getMariaUser() {
        String value = getString("Database.MariaDB.User", "luckprefix");
        getDebugLog().debug("DatabaseConfig.getMariaUser -> " + value);
        return value;
    }

    public String getMariaPassword() {
        String value = getString("Database.MariaDB.Password", "");
        String masked = value.isEmpty() ? "(empty)" : "******";
        getDebugLog().debug("DatabaseConfig.getMariaPassword -> " + masked);
        return value;
    }

    public String getMongoHost() {
        String value = getString("Database.MongoDB.Host", "localhost");
        getDebugLog().debug("DatabaseConfig.getMongoHost -> " + value);
        return value;
    }

    public int getMongoPort() {
        int value = getInt("Database.MongoDB.Port", 27017);
        getDebugLog().debug("DatabaseConfig.getMongoPort -> " + value);
        return value;
    }

    public String getMongoDatabase() {
        String value = getString("Database.MongoDB.Database", "luckprefix");
        getDebugLog().debug("DatabaseConfig.getMongoDatabase -> " + value);
        return value;
    }

    public String getMongoUser() {
        String value = getString("Database.MongoDB.User", "luckprefix");
        getDebugLog().debug("DatabaseConfig.getMongoUser -> " + value);
        return value;
    }

    public String getMongoPassword() {
        String value = getString("Database.MongoDB.Password", "");
        String masked = value.isEmpty() ? "(empty)" : "******";
        getDebugLog().debug("DatabaseConfig.getMongoPassword -> " + masked);
        return value;
    }
}