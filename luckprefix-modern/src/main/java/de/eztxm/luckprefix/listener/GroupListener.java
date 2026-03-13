package de.eztxm.luckprefix.listener;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.luckprefix.api.event.IGroupListener;
import de.eztxm.luckprefix.api.manager.IGroupManager;
import de.eztxm.luckprefix.api.manager.IPlayerManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.group.GroupCreateEvent;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.group.GroupDeleteEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class GroupListener implements IGroupListener {

    private final LuckPrefix plugin;
    private final LuckPerms luckPerms;
    private final IGroupManager groupManager;
    private final IPlayerManager playerManager;
    private final List<EventSubscription<?>> subscriptions = new CopyOnWriteArrayList<>();

    public GroupListener(LuckPrefix plugin, LuckPerms luckPerms, IGroupManager groupManager, IPlayerManager playerManager) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
        this.groupManager = groupManager;
        this.playerManager = playerManager;
    }

    @Override
    public void onCreateGroup() {
        EventBus eventBus = this.luckPerms.getEventBus();
        subscriptions.add(eventBus.subscribe(plugin, GroupCreateEvent.class,
                event -> this.groupManager.createGroup(event.getGroup().getName())));
    }

    @Override
    public void onDeleteGroup() {
        EventBus eventBus = this.luckPerms.getEventBus();
        subscriptions.add(eventBus.subscribe(plugin, GroupDeleteEvent.class,
                event -> this.groupManager.deleteGroup(event.getGroupName())));
    }

    @Override
    public void onUpdateGroup() {
        EventBus eventBus = this.luckPerms.getEventBus();
        subscriptions.add(eventBus.subscribe(plugin, GroupDataRecalculateEvent.class, event -> {
            this.groupManager.deleteGroup(event.getGroup().getName());
            this.groupManager.createGroup(event.getGroup().getName());
        }));
    }

    @Override
    public void onUpdateUserGroup() {
        EventBus eventBus = this.luckPerms.getEventBus();
        subscriptions.add(eventBus.subscribe(plugin, UserDataRecalculateEvent.class,
                event -> this.playerManager.setUserGroup(event.getUser().getUniqueId(), event.getUser().getPrimaryGroup())));
    }

    @Override
    public void close() {
        for (EventSubscription<?> subscription : subscriptions) {
            try {
                subscription.close();
            } catch (Exception ignored) {
            }
        }
        subscriptions.clear();
    }
}
