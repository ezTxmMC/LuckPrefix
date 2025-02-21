package de.eztxm.luckprefix.common.util.database;

import de.eztxm.ezlib.object.ObjectConverter;

public interface Processor {

    boolean isGroupExists(String group);
    ObjectConverter getGroupValue(String group, String key);
    void updateGroup(String group, String key, Object value);
    void addGroup(String group, String prefix, String suffix, String chatformat, String tabformat, int sortId, String color);
    void removeGroup(String group);

}
