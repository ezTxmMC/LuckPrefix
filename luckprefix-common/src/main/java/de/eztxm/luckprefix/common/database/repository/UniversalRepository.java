package de.eztxm.luckprefix.common.database.repository;

import de.eztxm.luckprefix.common.database.connections.DatabaseConnection;
import de.eztxm.luckprefix.common.database.repository.strategy.MariaDBRepositoryStrategy;
import de.eztxm.luckprefix.common.database.repository.strategy.MongoDBRepositoryStrategy;
import de.eztxm.luckprefix.common.database.repository.strategy.RepositoryStrategy;
import de.eztxm.luckprefix.common.database.repository.strategy.SQLiteRepositoryStrategy;
import de.eztxm.luckprefix.common.database.util.DatabaseType;

import java.lang.reflect.ParameterizedType;

public abstract class UniversalRepository<T, ID> {

    protected final Class<T> entityClass;
    protected final DatabaseConnection connection;
    protected final DatabaseType databaseType;
    private final RepositoryStrategy<T, ID> strategy;

    public UniversalRepository(DatabaseConnection connection) {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.connection = connection;
        this.databaseType = connection.getType();

        switch (databaseType) {
            case MONGODB -> this.strategy = new MongoDBRepositoryStrategy<>(entityClass, connection);
            case SQLITE -> this.strategy = new SQLiteRepositoryStrategy<>(entityClass, connection);
            case MARIADB -> this.strategy = new MariaDBRepositoryStrategy<>(entityClass, connection);
            default -> throw new IllegalArgumentException("Unsupported database type");
        }
    }
}
