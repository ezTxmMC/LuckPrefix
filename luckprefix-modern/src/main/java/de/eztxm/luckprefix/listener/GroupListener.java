package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupCreateEvent;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.group.GroupDeleteEvent;

public class GroupListener {

    public void onCreateGroup() {
        EventBus eventBus = LuckPrefix.getInstance().getLuckPerms().getEventBus();
        eventBus.subscribe(GroupCreateEvent.class, event -> LuckPrefix.getInstance().getGroupManager().createGroup(event.getGroup().getName()));
    }

    public void onReloadGroup() {
        EventBus eventBus = LuckPrefix.getInstance().getLuckPerms().getEventBus();
        eventBus.subscribe(GroupDataRecalculateEvent.class, event -> {
            LuckPrefix.getInstance().getGroupManager().deleteGroup(event.getGroup().getName());
            LuckPrefix.getInstance().getGroupManager().createGroup(event.getGroup().getName());
        });
    }

    public void onDeleteGroup() {
        EventBus eventBus = LuckPrefix.getInstance().getLuckPerms().getEventBus();
        eventBus.subscribe(GroupDeleteEvent.class, event -> LuckPrefix.getInstance().getGroupManager().deleteGroup(event.getGroupName()));
    }
}
