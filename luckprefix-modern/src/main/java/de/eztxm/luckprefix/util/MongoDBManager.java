package de.eztxm.luckprefix.util;

import de.eztxm.database.MongoDBConnection;
import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.database.MongoDBProcessor;
import org.bukkit.configuration.file.FileConfiguration;

public class MongoDBManager {
    private final MongoDBProcessor processor;

    public MongoDBManager(MongoDBConnection connection) {
        this.processor = new MongoDBProcessor(connection);
    }

    public static MongoDBConnection createMongoDBConnection() {
        FileConfiguration config = LuckPrefix.getInstance().getConfig();
        return new MongoDBConnection(
                config.getString("Database.MariaDB.Host"),
                config.getInt("Database.MariaDB.Port"),
                config.getString("Database.MariaDB.User"),
                config.getString("Database.MariaDB.Password"),
                config.getString("Database.MariaDB.Database")
        );
    }
}
