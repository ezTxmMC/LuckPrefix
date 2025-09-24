package de.eztxm.luckprefix.common.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
public abstract class AbstractConfig {

    private final Path filePath;
    private final Map<String, List<String>> commentMap = new LinkedHashMap<>();
    private Map<String, Object> dataTree = new LinkedHashMap<>();
    private boolean defaultsWereApplied = false;

    protected AbstractConfig(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath, "filePath");
    }

    private static Map<String, Object> loadYamlToMap(File sourceFile) {
        if (!sourceFile.exists()) {
            return new LinkedHashMap<>();
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8)) {
            Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions().setProcessComments(false)));
            Object root = yaml.load(reader);
            if (root instanceof Map) {
                return deepCopyMap((Map<?, ?>) root);
            }
            return new LinkedHashMap<>();
        } catch (IOException ioException) {
            return new LinkedHashMap<>();
        }
    }

    private static void dumpMapToYaml(File targetFile,
                                      Map<String, Object> content,
                                      Map<String, List<String>> comments) {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(false);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setIndent(2);
        dumperOptions.setIndicatorIndent(1);
        dumperOptions.setSplitLines(false);

        Representer representer = new Representer(dumperOptions);
        Yaml yaml = new Yaml(new SafeConstructor(loaderOptions), representer, dumperOptions);

        Map<String, Object> composite = new LinkedHashMap<>();
        if (comments != null && !comments.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : comments.entrySet()) {
                String commentKey = entry.getKey();
                List<String> lines = entry.getValue();
                if (commentKey != null && lines != null && !lines.isEmpty()) {
                    composite.put("# " + commentKey, String.join("\n# ", lines));
                }
            }
        }
        if (content != null) {
            composite.putAll(content);
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8)) {
            yaml.dump(composite, writer);
        } catch (IOException ignored) {}
    }

    private static Map<String, Object> deepCopyMap(Map<?, ?> source) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            String key = entry.getKey() == null ? "null" : String.valueOf(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map) {
                copy.put(key, deepCopyMap((Map<?, ?>) value));
            } else {
                copy.put(key, value);
            }
        }
        return copy;
    }

    protected abstract void defineDefaults();

    protected void afterLoad() {
    }

    public final Path path() {
        return filePath;
    }

    public synchronized void load() {
        ensureParentDirectory();
        dataTree = loadYamlToMap(filePath.toFile());
        if (!defaultsWereApplied) {
            defineDefaults();
            defaultsWereApplied = true;
        }
        afterLoad();
    }

    public synchronized void reload() {
        load();
    }

    public synchronized void save() {
        ensureParentDirectory();
        dumpMapToYaml(filePath.toFile(), dataTree, commentMap);
    }

    public synchronized long lastModifiedMillis() {
        File targetFile = filePath.toFile();
        return targetFile.exists() ? targetFile.lastModified() : 0L;
    }

    private void ensureParentDirectory() {
        File parentDirectory = filePath.toFile().getParentFile();
        if (parentDirectory != null) {

            parentDirectory.mkdirs();
        }
    }

    public synchronized void addDefault(String dottedPath, Object defaultValue) {
        if (isSet(dottedPath)) {
            return;
        }
        set(dottedPath, defaultValue);
    }

    public synchronized void setComments(String dottedPath, List<String> commentLines) {
        commentMap.put(dottedPath, commentLines);
    }

    public synchronized void saveDefaults() {
        save();
    }

    public synchronized void saveComments() {
        save();
    }

    public synchronized boolean contains(String dottedPath) {
        return isSet(dottedPath);
    }

    public synchronized boolean isSet(String dottedPath) {
        return get(dottedPath) != null;
    }

    public synchronized Object get(String dottedPath) {
        String[] pathParts = dottedPath.split("\\.");
        Map<String, Object> currentNode = dataTree;
        for (int index = 0; index < pathParts.length; index++) {
            String part = pathParts[index];
            boolean isLast = index == pathParts.length - 1;
            Object next = currentNode.get(part);
            if (isLast) {
                return next;
            }
            if (!(next instanceof Map)) {
                return null;
            }
            currentNode = (Map<String, Object>) next;
        }
        return null;
    }

    public synchronized String getString(String dottedPath, String defaultValue) {
        Object raw = get(dottedPath);
        return raw == null ? defaultValue : String.valueOf(raw);
    }

    public synchronized int getInt(String dottedPath, int defaultValue) {
        Object raw = get(dottedPath);
        if (raw instanceof Number) return ((Number) raw).intValue();
        if (raw instanceof String) {
            try {
                return Integer.parseInt((String) raw);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    public synchronized boolean getBoolean(String dottedPath, boolean defaultValue) {
        Object raw = get(dottedPath);
        if (raw instanceof Boolean) return (Boolean) raw;
        if (raw instanceof String) return Boolean.parseBoolean((String) raw);
        return defaultValue;
    }

    public synchronized void set(String dottedPath, Object value) {
        String[] pathParts = dottedPath.split("\\.");
        Map<String, Object> currentNode = dataTree;
        for (int index = 0; index < pathParts.length - 1; index++) {
            String part = pathParts[index];
            Object next = currentNode.get(part);
            if (!(next instanceof Map)) {
                Map<String, Object> newChild = new LinkedHashMap<>();
                currentNode.put(part, newChild);
                currentNode = newChild;
            } else {
                currentNode = (Map<String, Object>) next;
            }
        }
        currentNode.put(pathParts[pathParts.length - 1], value);
    }
}