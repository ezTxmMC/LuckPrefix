package de.eztxm.luckprefix.common.database.repository.strategy;

import de.eztxm.luckprefix.common.database.query.Query;

import java.util.List;
import java.util.Optional;

public interface RepositoryStrategy<T, ID> {
    void initialize();
    T save(T entity);
    List<T> saveAll(List<T> entities);
    Optional<T> findById(ID id);
    List<T> findAll();
    List<T> findAllById(List<ID> ids);
    boolean existsById(ID id);
    long count();
    void deleteById(ID id);
    void delete(T entity);
    void deleteAll();
    List<T> executeQuery(Query query);
    <R> R executeInTransaction(TransactionCallback<R> callback);
}
