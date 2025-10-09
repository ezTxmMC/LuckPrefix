package de.eztxm.luckprefix.api;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public interface ILuckPrefixAPI {

    AtomicReference<ILuckPrefixAPI> INSTANCE = new AtomicReference<>();

    static Optional<ILuckPrefixAPI> getInstance() {
        return Optional.ofNullable(INSTANCE.get());
    }

    static ILuckPrefixAPI initialize(ILuckPrefixAPI instance) {
        if(INSTANCE.compareAndSet(null, instance)) return instance;
        throw new IllegalArgumentException("LuckPrefix API can not be null");
    }


}
