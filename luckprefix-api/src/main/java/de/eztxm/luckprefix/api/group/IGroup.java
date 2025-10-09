package de.eztxm.luckprefix.api.group;

import de.eztxm.luckprefix.api.group.meta.*;

public interface IGroup {

    IGroupId id();
    IPrefix prefix();
    ISuffix suffix();
    ITabFormat tabFormat();
    IChatFormat chatFormat();
    INameColor nameColor();

    boolean setPrefix(IPrefix prefix);
    boolean setSuffix(ISuffix suffix);
    boolean setTabFormat(ITabFormat tabFormat);
    boolean setChatFormat(IChatFormat chatFormat);
    boolean setNameColor(INameColor nameColor);

}
