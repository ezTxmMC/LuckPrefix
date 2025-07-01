package de.eztxm.luckprefix.common.database.query;

import de.eztxm.luckprefix.common.database.query.cond.Condition;
import de.eztxm.luckprefix.common.database.query.sort.Sort;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private final List<Condition> conditions = new ArrayList<>();
    private final List<Sort> sorts = new ArrayList<>();
    private Integer limit;
    private Integer offset;

    public List<Condition> getConditions() {
        return conditions;
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void addCondition(Condition condition) {
        this.conditions.add(condition);
    }

    public void addSort(Sort sort) {
        this.sorts.add(sort);
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
