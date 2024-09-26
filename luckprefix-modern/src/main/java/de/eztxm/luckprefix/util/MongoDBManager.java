package de.eztxm.luckprefix.util;

import de.eztxm.ezlib.database.MongoDBConnection;
import de.eztxm.luckprefix.util.database.MongoDBProcessor;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class MongoDBManager {
    private final MongoDBProcessor processor;

    public MongoDBManager(MongoDBConnection connection) {
        this.processor = new MongoDBProcessor(connection);
    }

    public static MongoDBConnection createMongoDBConnection(FileConfiguration configuration) {
        return new MongoDBConnection(
                configuration.getString("Database.MongoDB.Host"),
                configuration.getInt("Database.MongoDB.Port"),
                configuration.getString("Database.MongoDB.User"),
                configuration.getString("Database.MongoDB.Password"),
                configuration.getString("Database.MongoDB.Database")
        );
    }
}
