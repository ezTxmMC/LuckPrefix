package de.eztxm.luckprefix.common.database.repository.strategy;

import de.eztxm.luckprefix.common.database.connections.DatabaseConnection;

public class MariaDBRepositoryStrategy implements RepositoryStrategy<T, ID> {

    public <T> MariaDBRepositoryStrategy(Class<T> entityClass, DatabaseConnection connection) {
    }
}
