package de.eztxm.luckprefix.api.event;

import de.eztxm.luckprefix.api.group.IGroup;

public record GroupEditEvent(IGroup group) implements IEvent {}
