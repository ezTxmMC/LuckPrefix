package de.eztxm.luckprefix.util.database;

import com.mongodb.client.model.Filters;
import de.eztxm.ezlib.database.MongoDBConnection;
import de.eztxm.object.ObjectConverter;
import org.bson.Document;

public class MongoDBProcessor implements Processor {
    private final MongoDBConnection connection;

    public MongoDBProcessor(MongoDBConnection connection) {
        this.connection = connection;
        this.connection.createCollection("luckprefix_groups");
    }

    @Override
    public boolean isGroupExists(String group) {
        return this.connection.find("luckprefix_groups", Filters.eq("name", group)) != null;
    }

    @Override
    public ObjectConverter getGroupValue(String group, String key) {
        if (!isGroupExists(group)) return null;
        return new ObjectConverter(this.connection.find("luckprefix_groups", Filters.eq("name", group)).get(key));
    }

    @Override
    public void updateGroup(String group, String key, Object value) {
        if (!isGroupExists(group)) return;
        Document document = getGroup(group);
        document.remove(key);
        document.put(key, value);
        this.connection.replace("luckprefix_groups", Filters.eq("name", group), document);
    }

    @Override
    public void addGroup(String group, String prefix, String suffix, String chatformat, String tabformat, int sortId, String color) {
        if (isGroupExists(group)) return;
        Document document = new Document();
        document.put("prefix", prefix);
        document.put("suffix", suffix);
        document.put("chatformat", chatformat);
        document.put("tabformat", tabformat);
        document.put("sortId", sortId);
        document.put("namecolor", color);
        this.connection.insert("luckprefix_groups", document);
    }

    @Override
    public void removeGroup(String group) {
        if (!isGroupExists(group)) return;
        this.connection.delete("luckprefix_groups", Filters.eq("name", group));
    }

    private Document getGroup(String group) {
        if (!isGroupExists(group)) return null;
        return this.connection.find("luckprefix_groups", Filters.eq("name", group));
    }
}
