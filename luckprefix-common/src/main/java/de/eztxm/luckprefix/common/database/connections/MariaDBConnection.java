package de.eztxm.luckprefix.common.database.connections;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.eztxm.luckprefix.common.database.util.DatabaseConfig;
import de.eztxm.luckprefix.common.database.util.DatabaseType;

import java.sql.Connection;
import java.sql.SQLException;

public class MariaDBConnection extends DatabaseConnection {
    private HikariDataSource dataSource;

    public MariaDBConnection(DatabaseConfig config) {
        super(config);
        connect();
    }

    @Override
    public void connect() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mariadb://" + config.getConnectionString());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(6000000);
        hikariConfig.setMaxLifetime(18000000);

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void close() {
        if(dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    @Override
    public Object getNativeConnection() {
        return dataSource;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MARIADB;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
