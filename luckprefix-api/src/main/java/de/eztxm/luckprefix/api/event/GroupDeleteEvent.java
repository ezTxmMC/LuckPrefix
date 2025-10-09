package de.eztxm.luckprefix.api.event;

import de.eztxm.luckprefix.api.group.IGroup;

public record GroupDeleteEvent(IGroup group) implements IEvent {}
