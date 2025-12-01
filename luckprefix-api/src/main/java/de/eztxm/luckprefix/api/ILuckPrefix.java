package de.eztxm.luckprefix.api;

import de.eztxm.luckprefix.api.config.AbstractConfig;

public interface ILuckPrefix {

    void loaded();
    void enabled();
    void disabled();
    void startConfigWatcher(AbstractConfig abstractConfig);

    static ILuckPrefix get() {
        return LuckPrefixProvider.instance;
    }

    static void register(ILuckPrefix instance) {
        LuckPrefixProvider.instance = instance;
    }

    final class LuckPrefixProvider {
        private static ILuckPrefix instance;

        private LuckPrefixProvider() {
            throw new UnsupportedOperationException("Cannot instantiate provider");
        }
    }
}
