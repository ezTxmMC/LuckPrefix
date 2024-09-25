package de.eztxm.luckprefix.util.database;

import de.eztxm.api.database.SQLConnection;
import de.eztxm.luckprefix.database.sql.Delete;
import de.eztxm.luckprefix.database.sql.Insert;
import de.eztxm.luckprefix.database.sql.Table;
import de.eztxm.object.ObjectConverter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLDatabaseProcessor {
    private final SQLConnection connection;

    public SQLDatabaseProcessor(SQLConnection connection) {
        this.connection = connection;
        this.createTable(Table.CREATE_GROUPS_TABLE);
        this.createTable(Table.CREATE_MESSAGES_TABLE);
    }

    public void createTable(Table table) {
        this.connection.put(table.getQuery());
    }

    public boolean isGroupExists(String group) {
        try (ResultSet resultSet = this.connection.queryAsync("select `group` from `luckprefix_groups` where `group`='?'", group).join()) {
            return resultSet.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public ObjectConverter getGroupValue(String group, String key) {
        if (!isGroupExists(group)) return null;
        try (ResultSet resultSet = this.connection.queryAsync("select `?` from `luckprefix_groups` where `group`='?'", key, group).join()) {
            if (resultSet.next()) {
                return new ObjectConverter(resultSet.getObject(key));
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    public void updateGroup(String group, String key, Object value) {
        if (!isGroupExists(group)) return;
        this.connection.queryAsync("update `luckprefix_groups` set `?`='?' where `group`='?'", key, value, group).join();
    }

    public void addGroup(String group, String prefix, String suffix, String chatformat, String tabformat, String sortId, String color) {
        if (isGroupExists(group)) return;
        this.connection.putAsync(Insert.INSERT_GROUP.getQuery().formatted(group, prefix, suffix, chatformat, tabformat, sortId, color)).join();
    }

    public void removeGroup(String group) {
        if (!isGroupExists(group)) return;
        this.connection.putAsync(Delete.DELETE_GROUP.getQuery().formatted(group)).join();
    }
}
