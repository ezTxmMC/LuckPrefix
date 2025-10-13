package de.eztxm.luckprefix.api;

import de.eztxm.luckprefix.api.config.AbstractConfig;

public interface ILuckPrefixAPI {

    void loaded();
    void enabled();
    void disabled();
    void startConfigWatcher(AbstractConfig abstractConfig);

}
