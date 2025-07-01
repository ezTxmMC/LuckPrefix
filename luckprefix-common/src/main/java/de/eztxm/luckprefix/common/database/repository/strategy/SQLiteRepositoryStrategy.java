package de.eztxm.luckprefix.common.database.repository.strategy;

import de.eztxm.luckprefix.common.database.connections.DatabaseConnection;

public class SQLiteRepositoryStrategy implements RepositoryStrategy<T, ID> {

    public <T> SQLiteRepositoryStrategy(Class<T> entityClass, DatabaseConnection connection) {
    }
}
