package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupCreateEvent;
import net.luckperms.api.event.group.GroupDeleteEvent;

public class GroupListener {

    public void createGroup() {
        EventBus eventBus = LuckPrefix.getInstance().getLuckPerms().getEventBus();
        eventBus.subscribe(GroupCreateEvent.class, event -> LuckPrefix.getInstance().getGroups().createGroup(event.getGroup()));
    }

    public void deleteGroup() {
        EventBus eventBus = LuckPrefix.getInstance().getLuckPerms().getEventBus();
        eventBus.subscribe(GroupDeleteEvent.class, event -> LuckPrefix.getInstance().getGroups().deleteGroup(event.getGroupName()));
    }
}
