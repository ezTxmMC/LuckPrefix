package de.eztxm.luckprefix.api.event;

import java.util.function.Consumer;

public interface IEventBus {

    <T extends IEvent> AutoCloseable subscribe(Class<T> type, Consumer<T> listener);

    void publish(IEvent event);

}
