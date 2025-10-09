package de.eztxm.luckprefix.api.event;

public sealed interface IEvent permits GroupCreateEvent, GroupDeleteEvent, GroupEditEvent {}
