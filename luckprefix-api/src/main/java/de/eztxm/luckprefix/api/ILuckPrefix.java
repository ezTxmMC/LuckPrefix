package de.eztxm.luckprefix.api;

import de.eztxm.luckprefix.api.config.AbstractConfig;

public interface ILuckPrefix {

    void loaded();
    void enabled();
    void disabled();
    void startConfigWatcher(AbstractConfig abstractConfig);

    static ILuckPrefix get() {
        ILuckPrefix inst = LuckPrefixProvider.instance;
        if (inst == null) {
            throw new IllegalStateException("LuckPrefix has not been registered yet");
        }
        return inst;
    }

    static void register(ILuckPrefix newInstance) {
        if (newInstance == null) {
            throw new IllegalArgumentException("Instance cannot be null");
        }
        synchronized (LuckPrefixProvider.class) {
            LuckPrefixProvider.instance = newInstance;
        }
    }

    final class LuckPrefixProvider {
        private static volatile ILuckPrefix instance;

        private LuckPrefixProvider() {
            throw new UnsupportedOperationException("Cannot instantiate provider");
        }
    }
}

