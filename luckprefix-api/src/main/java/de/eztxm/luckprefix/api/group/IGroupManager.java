package de.eztxm.luckprefix.api.group;

import de.eztxm.luckprefix.api.group.meta.IGroupId;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface IGroupManager {

    Optional<IGroup> find(String groupId);

    Optional<IGroup> find(IGroupId groupId);

    IGroup getOrCreate(String groupId);

    Set<IGroup> list();

    boolean create(IGroup group);

    boolean delete(IGroup group);

    default CompletableFuture<IGroup> getOrCreateAsync(String groupId) {
        return CompletableFuture.supplyAsync(() -> getOrCreate(groupId));
    }

}
