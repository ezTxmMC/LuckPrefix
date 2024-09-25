package de.eztxm.luckprefix.util.database;

import com.mongodb.client.model.Filters;
import de.eztxm.database.MongoDBConnection;
import org.bson.Document;

public class MongoDBProcessor {
    private final MongoDBConnection connection;

    public MongoDBProcessor(MongoDBConnection connection) {
        this.connection = connection;
        this.createCollections();
    }

    public void createCollections() {
        this.connection.createCollection("luckprefix_groups");
    }

    public boolean isGroupExists(String group) {
        return this.connection.find("luckprefix_groups", Filters.eq("name", group)) != null;
    }

    public Document getGroup(String group) {
        if (!isGroupExists(group)) return null;
        return this.connection.find("luckprefix_groups", Filters.eq("name", group));
    }

    public void updateGroup(String group, Document document) {
        if (!isGroupExists(group)) return;
        this.connection.update("luckprefix_groups", Filters.eq("name", group), document);
    }

    public void addGroup(String group, Document document) {
        if (isGroupExists(group)) return;
        this.connection.insert("luckprefix_groups", document);
    }

    public void removeGroup(String group) {
        if (!isGroupExists(group)) return;
        this.connection.delete("luckprefix_groups", Filters.eq("name", group));
    }
}
