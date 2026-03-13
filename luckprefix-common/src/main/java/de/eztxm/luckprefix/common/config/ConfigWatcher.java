package de.eztxm.luckprefix.common.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Predicate;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public final class ConfigWatcher {

    private final File directoryToWatch;
    private final Set<String> watchedFileNamesLower;
    private final Predicate<Path> onFileChanged;
    private final long debounceMillis;

    private final ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "LuckPrefix-ConfigWatcher");
                thread.setDaemon(true);
                return thread;
            });
    private final ConcurrentMap<String, Long> lastTriggerByFile = new ConcurrentHashMap<>();
    private WatchService watchService;
    private Future<?> watchTask;

    public ConfigWatcher(File directoryToWatch,
                         Set<String> watchedFileNames,
                         Predicate<Path> onFileChanged,
                         long debounceMillis) {
        this.directoryToWatch = Objects.requireNonNull(directoryToWatch, "directoryToWatch");
        this.onFileChanged = Objects.requireNonNull(onFileChanged, "onFileChanged");
        this.debounceMillis = Math.max(0, debounceMillis);

        this.watchedFileNamesLower = Objects.requireNonNull(watchedFileNames, "watchedFileNames")
                .stream().map(s -> s.toLowerCase().trim()).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public ConfigWatcher(File directoryToWatch, Predicate<Path> onFileChanged) {
        this(directoryToWatch,
                Set.of("config.yml", "database.yml", "groups.yml"),
                onFileChanged,
                250L);
    }

    public void start() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            this.directoryToWatch.toPath().register(watchService, ENTRY_MODIFY, ENTRY_CREATE);
        } catch (IOException ioException) {

            return;
        }

        this.watchTask = executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey watchKey;
                try {
                    watchKey = watchService.take();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    break;
                }
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    if (event.kind() != ENTRY_MODIFY && event.kind() != ENTRY_CREATE) continue;

                    Object rawContext = event.context();
                    if (!(rawContext instanceof Path)) continue;

                    Path relativePath = (Path) rawContext;
                    String fileNameLower = relativePath.getFileName().toString().toLowerCase();

                    if (!watchedFileNamesLower.contains(fileNameLower)) continue;

                    long nowMillis = System.currentTimeMillis();
                    Long lastMillis = lastTriggerByFile.get(fileNameLower);
                    if (lastMillis != null && (nowMillis - lastMillis) < debounceMillis) continue;
                    lastTriggerByFile.put(fileNameLower, nowMillis);

                    Path absolutePath = directoryToWatch.toPath().resolve(relativePath).toAbsolutePath();

                    try {
                        onFileChanged.test(absolutePath);
                    } catch (Exception ignored) {

                    }
                }
                watchKey.reset();
            }
        });
    }

    public void stop() {
        if (watchTask != null) watchTask.cancel(true);
        try {
            if (watchService != null) watchService.close();
        } catch (IOException ignored) {
        }
        watchTask = null;
        watchService = null;
        executorService.shutdownNow();
        lastTriggerByFile.clear();
    }
}