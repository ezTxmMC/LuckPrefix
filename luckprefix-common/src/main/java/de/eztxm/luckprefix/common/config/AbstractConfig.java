package de.eztxm.luckprefix.common.config;

import de.eztxm.luckprefix.common.logging.DebugLog;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("unchecked")
public abstract class AbstractConfig {

    private final Path filePath;
    private final DebugLog debugLog;

    private Map<String, Object> dataTree = new LinkedHashMap<>();

    private List<String> headerComments = new ArrayList<>();
    private final Map<String, List<String>> perKeyComments = new LinkedHashMap<>();

    private boolean defaultsWereApplied = false;

    protected AbstractConfig(Path filePath, DebugLog debugLog) {
        this.filePath = Objects.requireNonNull(filePath, "filePath");
        this.debugLog = Objects.requireNonNull(debugLog, "debugLog");
        this.debugLog.info(getClass().getSimpleName() + ": constructed for " + filePath);
    }

    protected abstract void defineDefaults();

    protected void afterLoad() { }

    public final Path path() {
        return filePath;
    }


    public final DebugLog getDebugLog() {
        return debugLog;
    }

    public synchronized void load() {
        debugLog.info(getClass().getSimpleName() + ".load: begin - " + filePath);
        boolean existed = filePath.toFile().exists();
        ensureParentDirectoryExists();
        dataTree = loadYamlToMap(filePath.toFile(), debugLog);

        if (!defaultsWereApplied) {
            debugLog.debug(getClass().getSimpleName() + ".load: applying defaults (first run)");
            defineDefaults();
            defaultsWereApplied = true;
            if(!existed) save();
        }
        try {
            afterLoad();
            debugLog.debug(getClass().getSimpleName() + ".load: afterLoad hook done");
        } catch (Exception ex) {
            debugLog.error(getClass().getSimpleName() + ".load: afterLoad threw", ex);
        }
        debugLog.info(getClass().getSimpleName() + ".load: done");
    }

    public synchronized void reload() {
        debugLog.info(getClass().getSimpleName() + ".reload");
        load();
    }

    public synchronized void save() {
        debugLog.info(getClass().getSimpleName() + ".save: writing " + filePath);
        ensureParentDirectoryExists();
        writeYamlWithComments(filePath.toFile(), dataTree, headerComments, perKeyComments, debugLog);
        debugLog.info(getClass().getSimpleName() + ".save: done");
    }

    public synchronized void saveDefaults() {
        debugLog.debug(getClass().getSimpleName() + ".saveDefaults");
        save();
    }

    public synchronized void saveComments() {
        debugLog.debug(getClass().getSimpleName() + ".saveComments");
        save();
    }

    public synchronized long lastModifiedMillis() {
        File target = filePath.toFile();
        if (!target.exists()) return 0L;
        return target.lastModified();
    }

    private void ensureParentDirectoryExists() {
        File parent = filePath.toFile().getParentFile();
        if (parent == null) return;
        if (parent.exists()) return;
        boolean ok = parent.mkdirs();
        if (ok) debugLog.debug(getClass().getSimpleName() + ": created directory " + parent);
        if (!ok) debugLog.warn(getClass().getSimpleName() + ": failed to create directory " + parent);
    }

    private static Map<String, Object> loadYamlToMap(File sourceFile, DebugLog debugLog) {
        if (!sourceFile.exists()) {
            debugLog.debug("AbstractConfig.loadYamlToMap: file missing, returning empty map: " + sourceFile);
            return new LinkedHashMap<>();
        }

        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(false);

        try (Reader reader = new InputStreamReader(new FileInputStream(sourceFile), StandardCharsets.UTF_8)) {
            Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));
            Object root = yaml.load(reader);
            if (root instanceof Map) {
                Map<String, Object> map = deepCopyMap((Map<?, ?>) root);
                debugLog.debug("AbstractConfig.loadYamlToMap: loaded map with " + map.size() + " top-level keys from " + sourceFile.getName());
                return map;
            }
            debugLog.warn("AbstractConfig.loadYamlToMap: root is not a map for " + sourceFile.getName() + " → using empty map");
            return new LinkedHashMap<>();
        } catch (IOException io) {
            debugLog.error("AbstractConfig.loadYamlToMap: IO failure for " + sourceFile.getName(), io);
            return new LinkedHashMap<>();
        } catch (Exception ex) {
            debugLog.error("AbstractConfig.loadYamlToMap: parse failure for " + sourceFile.getName(), ex);
            return new LinkedHashMap<>();
        }
    }

    private static Map<String, Object> deepCopyMap(Map<?, ?> source) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            String key = entry.getKey() == null ? "null" : String.valueOf(entry.getKey());
            Object value = entry.getValue();
            Object out = value;
            if (value instanceof Map) out = deepCopyMap((Map<?, ?>) value);
            if (value instanceof List) out = deepCopyList((List<?>) value);
            copy.put(key, out);
        }
        return copy;
    }

    private static List<Object> deepCopyList(List<?> source) {
        List<Object> copy = new ArrayList<>(source.size());
        for (Object v : source) {
            Object out = v;
            if (v instanceof Map) out = deepCopyMap((Map<?, ?>) v);
            if (v instanceof List) out = deepCopyList((List<?>) v);
            copy.add(out);
        }
        return copy;
    }

    public synchronized void addDefault(String dottedPath, Object defaultValue) {
        boolean present = isSet(dottedPath);
        if (present) return;
        debugLog.info(getClass().getSimpleName() + ".addDefault: " + dottedPath + " = " + defaultValue);
        set(dottedPath, defaultValue);
    }

    public synchronized void setComments(String dottedPath, List<String> commentLines) {
        List<String> lines = new ArrayList<>();
        if (commentLines != null) lines.addAll(commentLines);
        perKeyComments.put(dottedPath, lines);
        debugLog.debug(getClass().getSimpleName() + ".setComments: " + dottedPath + " (" + lines.size() + " lines)");
    }

    public synchronized void setHeaderComments(List<String> lines) {
        headerComments = new ArrayList<>();
        if (lines != null) headerComments.addAll(lines);
        debugLog.debug(getClass().getSimpleName() + ".setHeaderComments: " + headerComments.size() + " lines");
    }

    public synchronized boolean contains(String dottedPath) {
        boolean present = isSet(dottedPath);
        debugLog.debug(getClass().getSimpleName() + ".contains: " + dottedPath + " -> " + present);
        return present;
    }

    public synchronized boolean isSet(String dottedPath) {
        Object v = get(dottedPath);
        return v != null;
    }

    public synchronized Object get(String dottedPath) {
        String[] parts = dottedPath.split("\\.");
        Map<String, Object> node = dataTree;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            boolean last = i == parts.length - 1;
            Object next = node.get(part);

            if (last) return next;
            if (!(next instanceof Map)) return null;
            node = (Map<String, Object>) next;
        }
        return null;
    }

    public synchronized String getString(String dottedPath, String defaultValue) {
        Object raw = get(dottedPath);
        if (raw == null) {
            debugLog.debug(getClass().getSimpleName() + ".getString: " + dottedPath + " -> <default> '" + defaultValue + "'");
            return defaultValue;
        }
        String out = String.valueOf(raw);
        debugLog.debug(getClass().getSimpleName() + ".getString: " + dottedPath + " -> '" + out + "'");
        return out;
    }

    public synchronized int getInt(String dottedPath, int defaultValue) {
        Object raw = get(dottedPath);
        if (raw instanceof Number) {
            int out = ((Number) raw).intValue();
            debugLog.debug(getClass().getSimpleName() + ".getInt: " + dottedPath + " -> " + out);
            return out;
        }
        if (raw instanceof String) {
            try {
                int parsed = Integer.parseInt((String) raw);
                debugLog.debug(getClass().getSimpleName() + ".getInt: " + dottedPath + " -> " + parsed + " (parsed)");
                return parsed;
            } catch (NumberFormatException ignored) {

            }
        }
        debugLog.debug(getClass().getSimpleName() + ".getInt: " + dottedPath + " -> <default> " + defaultValue);
        return defaultValue;
    }

    public synchronized boolean getBoolean(String dottedPath, boolean defaultValue) {
        Object raw = get(dottedPath);
        if (raw instanceof Boolean) {
            boolean out = (Boolean) raw;
            debugLog.debug(getClass().getSimpleName() + ".getBoolean: " + dottedPath + " -> " + out);
            return out;
        }
        if (raw instanceof String) {
            boolean out = Boolean.parseBoolean((String) raw);
            debugLog.debug(getClass().getSimpleName() + ".getBoolean: " + dottedPath + " -> " + out + " (parsed)");
            return out;
        }
        debugLog.debug(getClass().getSimpleName() + ".getBoolean: " + dottedPath + " -> <default> " + defaultValue);
        return defaultValue;
    }

    public synchronized void set(String dottedPath, Object value) {
        String[] parts = dottedPath.split("\\.");
        Map<String, Object> node = dataTree;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            Object next = node.get(part);
            boolean isMap = next instanceof Map;
            if (!isMap) {
                Map<String, Object> child = new LinkedHashMap<>();
                node.put(part, child);
                node = child;
                continue;
            }
            node = (Map<String, Object>) next;
        }
        node.put(parts[parts.length - 1], value);
        debugLog.info(getClass().getSimpleName() + ".set: " + dottedPath + " = " + value);
    }

    private static void writeYamlWithComments(
            File targetFile,
            Map<String, Object> root,
            List<String> header,
            Map<String, List<String>> comments,
            DebugLog debugLog
    ) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {

            if (header != null && !header.isEmpty()) {
                for (String line : header) {
                    writer.write("# " + line);
                    writer.newLine();
                }
                writer.newLine();
            }

            writeSection(writer, root, comments, "", "");
            debugLog.debug("AbstractConfig.writeYamlWithComments: wrote " + targetFile.getName());
        } catch (IOException ex) {
            debugLog.error("AbstractConfig.writeYamlWithComments: IO failure for " + targetFile.getName(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static void writeSection(
            BufferedWriter writer,
            Map<String, Object> section,
            Map<String, List<String>> comments,
            String parentPath,
            String indent
    ) throws IOException {
        if (section == null) return;

        for (Map.Entry<String, Object> entry : section.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String fullPath = parentPath.isEmpty() ? key : parentPath + "." + key;

            List<String> lines = comments.get(fullPath);
            if (lines != null && !lines.isEmpty()) {
                for (String line : lines) {
                    writer.write(indent);
                    writer.write("# ");
                    writer.write(line);
                    writer.newLine();
                }
            }

            if (value instanceof Map) {
                writer.write(indent + key + ":");
                writer.newLine();
                writeSection(writer, (Map<String, Object>) value, comments, fullPath, indent + "  ");
                continue;
            }

            if (value instanceof List) {
                writer.write(indent + key + ":");
                writer.newLine();
                for (Object item : (List<?>) value) {
                    writer.write(indent + "  - " + renderScalar(item));
                    writer.newLine();
                }
                continue;
            }

            writer.write(indent + key + ": " + renderScalar(value));
            writer.newLine();
        }
    }

    private static String renderScalar(Object value) {
        if (value == null) return "null";
        if (value instanceof Boolean || value instanceof Number) return String.valueOf(value);

        String raw = String.valueOf(value);
        boolean alreadyQuoted = (raw.startsWith("\"") && raw.endsWith("\"")) || (raw.startsWith("'") && raw.endsWith("'"));
        if (alreadyQuoted) return raw;

        boolean needsQuotes =
                raw.isEmpty() ||
                        raw.startsWith("#") ||
                        raw.matches(".*[:\\-?&*!|>'\"%@`\\[\\]{}].*") ||
                        raw.matches(".*\\s.*");

        if (!needsQuotes) return raw;

        String escaped = raw.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }
}