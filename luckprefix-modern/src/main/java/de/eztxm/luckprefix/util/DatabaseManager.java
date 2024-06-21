package de.eztxm.luckprefix.util;

import de.eztxm.api.database.SQLConnection;
import de.eztxm.database.MariaDBConnection;
import de.eztxm.database.SQLiteConnection;
import de.eztxm.luckprefix.LuckPrefix;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseManager {
    private final SQLConnection connection;

    public DatabaseManager(SQLConnection connection) {
        this.connection = connection;
    }

    public static SQLConnection createDatabaseConnection() {
        FileConfiguration config = LuckPrefix.getInstance().getConfig();
        String type = config.getString("Database.Type");
        return switch (type.toUpperCase()) {
            case "SQLITE" -> new SQLiteConnection(
                    config.getString("Database.SQLite.Path"),
                    config.getString("Database.SQLite.FileName")
            );
            case "MARIADB" -> new MariaDBConnection(
                    config.getString("Database.MariaDB.Host"),
                    config.getInt("Database.MariaDB.Port"),
                    config.getString("Database.MariaDB.Database"),
                    config.getString("Database.MariaDB.User"),
                    config.getString("Database.MariaDB.Password")
            );
            default -> null;
        };
    }
}
