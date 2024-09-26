package de.eztxm.luckprefix.util.database;

import de.eztxm.api.database.SQLConnection;
import de.eztxm.luckprefix.database.sql.*;
import de.eztxm.object.ObjectConverter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLDatabaseProcessor implements Processor {
    private final SQLConnection connection;

    public SQLDatabaseProcessor(SQLConnection connection) {
        this.connection = connection;
        this.createTable(Table.CREATE_GROUPS_TABLE);
        this.createTable(Table.CREATE_MESSAGES_TABLE);
        try (ResultSet resultSet = this.connection.query("select * from `luckprefix_groups`")) {
            if (resultSet.next()) {
                System.out.println(resultSet.getString("group"));
            }
        } catch (SQLException e) {
            e.fillInStackTrace();
        }
    }

    public void createTable(Table table) {
        this.connection.put(table.getQuery());
    }

    @Override
    public boolean isGroupExists(String group) {
        System.out.println(group.toLowerCase());
        try (ResultSet resultSet = this.connection.query(Select.SELECT_GROUP.getQuery().formatted(group.toLowerCase()))) {
            if (resultSet.next()) {
                System.out.println(resultSet.getString("group"));
                System.out.println("TRUE");
                return resultSet.getString("group") != null;
            }
        } catch (SQLException ignored) {}
        System.out.println("FALSE");
        return false;
    }

    @Override
    public ObjectConverter getGroupValue(String group, String key) {
        if (!isGroupExists(group)) return null;
        try (ResultSet resultSet = this.connection.query(Select.SELECT_GROUP_VALUE.getQuery().formatted(key, group))) {
            if (resultSet.next()) {
                return new ObjectConverter(resultSet.getObject(key));
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public void updateGroup(String group, String key, Object value) {
        if (!isGroupExists(group)) return;
        this.connection.put(Update.UPDATE_GROUP.getQuery().formatted(key, value, group));
    }

    @Override
    public void addGroup(String group, String prefix, String suffix, String chatformat, String tabformat, int sortId, String color) {
        if (isGroupExists(group)) return;
        this.connection.put(Insert.INSERT_GROUP.getQuery().formatted(group, prefix, suffix, chatformat, tabformat, sortId, color));
    }

    @Override
    public void removeGroup(String group) {
        if (!isGroupExists(group)) return;
        this.connection.put(Delete.DELETE_GROUP.getQuery().formatted(group));
    }
}
