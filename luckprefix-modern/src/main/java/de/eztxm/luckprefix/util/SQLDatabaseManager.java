package de.eztxm.luckprefix.util;

import de.eztxm.ezlib.api.database.SQLConnection;
import de.eztxm.ezlib.database.MariaDBConnection;
import de.eztxm.ezlib.database.SQLiteConnection;
import de.eztxm.luckprefix.common.util.database.SQLDatabaseProcessor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class SQLDatabaseManager {
    private final SQLDatabaseProcessor processor;

    public SQLDatabaseManager(SQLConnection connection) {
        this.processor = new SQLDatabaseProcessor(connection);
    }

    @SneakyThrows
    public static SQLConnection createSQLDatabaseConnection(FileConfiguration configuration) {
        String type = configuration.getString("Database.Type");
        switch (type.toUpperCase()) {
            case "SQLITE" -> {
                Class.forName("org.sqlite.JDBC");
                return new SQLiteConnection(
                        configuration.getString("Database.SQLite.Path"),
                        configuration.getString("Database.SQLite.FileName")
                );
            }
            case "MARIADB" -> {
                Class.forName("org.mariadb.jdbc.Driver");
                return new MariaDBConnection(
                        configuration.getString("Database.MariaDB.Host"),
                        configuration.getInt("Database.MariaDB.Port"),
                        configuration.getString("Database.MariaDB.Database"),
                        configuration.getString("Database.MariaDB.User"),
                        configuration.getString("Database.MariaDB.Password")
                );
            }
            default -> {
                return null;
            }
        }
    }
}
