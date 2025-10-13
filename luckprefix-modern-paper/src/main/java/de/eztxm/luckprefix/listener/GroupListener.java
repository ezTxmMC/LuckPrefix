package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.api.group.IGroupManager;
import de.eztxm.luckprefix.group.GroupManager;
import de.eztxm.luckprefix.util.PlayerManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupCreateEvent;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.group.GroupDeleteEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;

public record GroupListener(LuckPerms luckPerms, IGroupManager groupManager, PlayerManager playerManager) {

    public void onCreateGroup() {
        EventBus eventBus = this.luckPerms.getEventBus();
        eventBus.subscribe(GroupCreateEvent.class, event -> this.groupManager.createGroup(event.getGroup().getName()));
    }

    public void onDeleteGroup() {
        EventBus eventBus = this.luckPerms.getEventBus();
        eventBus.subscribe(GroupDeleteEvent.class, event -> this.groupManager.deleteGroup(event.getGroupName()));
    }

    public void onUpdateGroup() {
        EventBus eventBus = this.luckPerms.getEventBus();
        eventBus.subscribe(GroupDataRecalculateEvent.class, event -> {
            this.groupManager.deleteGroup(event.getGroup().getName());
            this.groupManager.createGroup(event.getGroup().getName());
        });
    }

    public void onUpdateUserGroup() {
        EventBus eventBus = this.luckPerms.getEventBus();
        eventBus.subscribe(UserDataRecalculateEvent.class, event -> this.playerManager.setUserGroup(event.getUser().getUniqueId(), event.getUser().getPrimaryGroup()));
    }
}
