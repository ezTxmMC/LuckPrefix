package de.eztxm.luckprefix.common.metadata;

import net.luckperms.api.model.group.Group;

public class LuckPermsDataHandler {

    public String getPrefix(Group group) {
        if (group == null) {
            return null;
        }
        return group.getCachedData().getMetaData().getPrefix();
    }

    public String getSuffix(Group group) {
        if (group == null) {
            return null;
        }
        return group.getCachedData().getMetaData().getSuffix();
    }
}
