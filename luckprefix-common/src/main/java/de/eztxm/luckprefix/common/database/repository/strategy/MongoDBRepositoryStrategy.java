package de.eztxm.luckprefix.common.database.repository.strategy;

import de.eztxm.luckprefix.common.database.connections.DatabaseConnection;

public class MongoDBRepositoryStrategy implements RepositoryStrategy<T, ID> {

    public <T> MongoDBRepositoryStrategy(Class<T> entityClass, DatabaseConnection connection) {
    }
}
