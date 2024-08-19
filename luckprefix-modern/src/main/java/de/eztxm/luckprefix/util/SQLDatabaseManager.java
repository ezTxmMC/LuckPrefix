package de.eztxm.luckprefix.util;

import de.eztxm.api.database.SQLConnection;
import de.eztxm.database.MariaDBConnection;
import de.eztxm.database.SQLiteConnection;
import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.database.SQLDatabaseProcessor;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class SQLDatabaseManager {
    private final SQLDatabaseProcessor processor;

    public SQLDatabaseManager(SQLConnection connection) {
        this.processor = new SQLDatabaseProcessor(connection);
    }

    public static SQLConnection createSQLDatabaseConnection() {
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
