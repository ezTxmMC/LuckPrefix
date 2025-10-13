package de.eztxm.luckprefix.common.config;

import de.eztxm.luckprefix.api.config.AbstractConfig;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigService {

    private final Map<Class<? extends AbstractConfig>, AbstractConfig> instanceMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends AbstractConfig>, ConfigFactory<?>> factoryMap = new ConcurrentHashMap<>();
    private final Map<Class<? extends AbstractConfig>, Path> pathMap = new ConcurrentHashMap<>();

    public <T extends AbstractConfig> void register(Class<T> configClass, Path filePath, ConfigFactory<T> factory) {
        Objects.requireNonNull(configClass, "configClass");
        Objects.requireNonNull(filePath, "filePath");
        Objects.requireNonNull(factory, "factory");
        pathMap.put(configClass, filePath);
        factoryMap.put(configClass, factory);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractConfig> T of(Class<T> configClass) {
        AbstractConfig existing = instanceMap.get(configClass);
        if (existing != null) return (T) existing;

        ConfigFactory<?> factory = factoryMap.get(configClass);
        Path filePath = pathMap.get(configClass);
        if (factory == null || filePath == null) {
            throw new IllegalStateException("No factory/path registered for " + configClass.getSimpleName());
        }

        AbstractConfig created = ((ConfigFactory<T>) factory).create(filePath);
        created.load();
        instanceMap.put(configClass, created);
        return (T) created;
    }

    public <T extends AbstractConfig> T instance(Class<T> configClass) {
        return of(configClass);
    }

    public <T extends AbstractConfig> void reload(Class<T> configClass) {
        T config = of(configClass);
        config.reload();
    }

    public void reloadAll() {
        for (AbstractConfig config : instanceMap.values()) {
            config.reload();
        }
    }

    public interface ConfigFactory<T extends AbstractConfig> {
        T create(Path path);
    }
}