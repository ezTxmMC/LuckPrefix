package de.eztxm.luckprefix.common.database.query.sort;

public class Sort {
    private final String field;
    private final SortOrder order;

    public Sort(String field, SortOrder order) {
        this.field = field;
        this.order = order;
    }

    public String getField() {
        return field;
    }

    public SortOrder getOrder() {
        return order;
    }
}
