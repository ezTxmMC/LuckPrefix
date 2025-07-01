package de.eztxm.luckprefix.common.database.connections;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.eztxm.luckprefix.common.database.util.DatabaseConfig;
import de.eztxm.luckprefix.common.database.util.DatabaseType;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteConnection extends DatabaseConnection {

    private HikariDataSource dataSource;

    public SQLiteConnection(DatabaseConfig config) {
        super(config);
        connect();
    }

    @Override
    public void connect() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + config.getConnectionString());
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setConnectionTestQuery("SELECT 1");

        hikariConfig.addDataSourceProperty("journal_mode", "WAL");
        hikariConfig.addDataSourceProperty("synchronous", "NORMAL");
        hikariConfig.addDataSourceProperty("temp_store", "MEMORY");
        hikariConfig.addDataSourceProperty("cache_size", "-64000");

        this.dataSource = new HikariDataSource(hikariConfig);

        try (Connection conn = dataSource.getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize SQLite", e);
        }
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
        return DatabaseType.SQLITE;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
