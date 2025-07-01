package de.eztxm.luckprefix.common.database.connections;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.eztxm.luckprefix.common.database.util.DatabaseConfig;
import de.eztxm.luckprefix.common.database.util.DatabaseType;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MongoConnection extends DatabaseConnection {

    private MongoClient client;
    private final Map<String, MongoDatabase> databases = new ConcurrentHashMap<>();

    public MongoConnection(DatabaseConfig config) {
        super(config);
        connect();
    }

    @Override
    public void connect() {
        CodecRegistry codecRegistry = fromRegistries(
                getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(config.getConnectionString()))
                .codecRegistry(codecRegistry)
                .build();

        this.client = MongoClients.create(settings);
    }

    @Override
    public void close() {
        if(client != null) {
            client.close();
        }
    }

    @Override
    public boolean isConnected() {
        try {
            client.listDatabaseNames().first();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object getNativeConnection() {
        return client;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MONGODB;
    }

    public MongoDatabase getDatabase(String name) {
        return databases.computeIfAbsent(name, client::getDatabase);
    }
}
