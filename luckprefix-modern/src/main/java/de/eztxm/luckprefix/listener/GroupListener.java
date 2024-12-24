package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.util.GroupManager;
import de.eztxm.luckprefix.util.PlayerManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupCreateEvent;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.group.GroupDeleteEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;

public class GroupListener {
    private final LuckPerms luckPerms;
    private final GroupManager groupManager;
    private final PlayerManager playerManager;

    public GroupListener(LuckPerms luckPerms, GroupManager groupManager, PlayerManager playerManager) {
        this.luckPerms = luckPerms;
        this.groupManager = groupManager;
        this.playerManager = playerManager;
    }

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
        eventBus.subscribe(UserDataRecalculateEvent.class, event ->
                this.playerManager.setUserGroup(event.getUser().getUniqueId(), event.getUser().getPrimaryGroup()));
    }
}
