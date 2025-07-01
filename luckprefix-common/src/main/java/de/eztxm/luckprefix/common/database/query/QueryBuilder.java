package de.eztxm.luckprefix.common.database.query;

import de.eztxm.luckprefix.common.database.query.cond.Condition;
import de.eztxm.luckprefix.common.database.query.cond.Operator;
import de.eztxm.luckprefix.common.database.query.sort.Sort;
import de.eztxm.luckprefix.common.database.query.sort.SortOrder;
import de.eztxm.luckprefix.common.database.repository.strategy.RepositoryStrategy;

import java.util.List;
import java.util.Optional;

public class QueryBuilder<T> {
    private final RepositoryStrategy<T, ?> strategy;
    private final Query query = new Query();

    public QueryBuilder(RepositoryStrategy<T, ?> strategy) {
        this.strategy = strategy;
    }

    public QueryBuilder<T> where(String field, Operator op, Object value) {
        query.addCondition(new Condition(field, op, value));
        return this;
    }

    public QueryBuilder<T> eq(String field, Object value) {
        return where(field, Operator.EQ, value);
    }

    public QueryBuilder<T> gt(String field, Object value) {
        return where(field, Operator.GT, value);
    }

    public QueryBuilder<T> gte(String field, Object value) {
        return where(field, Operator.GTE, value);
    }

    public QueryBuilder<T> lt(String field, Object value) {
        return where(field, Operator.LT, value);
    }

    public QueryBuilder<T> lte(String field, Object value) {
        return where(field, Operator.LTE, value);
    }

    public QueryBuilder<T> in(String field, Object value) {
        return where(field, Operator.IN, value);
    }

    public QueryBuilder<T> like(String field, Object value) {
        return where(field, Operator.LIKE, value);
    }

    public QueryBuilder<T> orderBy(String field, SortOrder value) {
        query.addSort(new Sort(field, value));
        return this;
    }

    public QueryBuilder<T> limit(int limit) {
        query.setLimit(limit);
        return this;
    }

    public QueryBuilder<T> offset(int offset) {
        query.setOffset(offset);
        return this;
    }

    public List<T> execute() {
        return strategy.executeQuery(query);
    }

    public Optional<T> first() {
        query.setLimit(1);
        List<T> results = execute();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

}
