package de.eztxm.luckprefix.util;

import de.eztxm.api.database.SQLConnection;
import de.eztxm.database.MariaDBConnection;
import de.eztxm.database.SQLiteConnection;
import de.eztxm.luckprefix.util.database.SQLDatabaseProcessor;
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
