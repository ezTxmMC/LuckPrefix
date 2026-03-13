package de.eztxm.luckprefix.api.event;

public interface IGroupListener extends AutoCloseable {

    void onCreateGroup();
    void onDeleteGroup();
    void onUpdateGroup();
    void onUpdateUserGroup();

    @Override
    default void close() {
    }

}
