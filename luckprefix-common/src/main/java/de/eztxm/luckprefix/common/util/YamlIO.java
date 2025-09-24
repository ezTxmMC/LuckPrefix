package de.eztxm.luckprefix.common.util;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class YamlIO {
    private YamlIO() {
    }

    public static Map<String, Object> load(File file) throws IOException {
        if (!file.exists()) return new LinkedHashMap<>();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions().setProcessComments(false)));
            Object object = yaml.load(reader);
            if (object instanceof Map) return deepCopy((Map<?, ?>) object);
            return new LinkedHashMap<>();
        }
    }

    public static Map<String, Object> loadFromString(String yamlText) throws IOException {
        File tmp = File.createTempFile("lp-default", ".yml");
        try {
            save(tmp, null);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
                writer.write(yamlText == null ? "" : yamlText);
            }
            return load(tmp);
        } finally {
            tmp.delete();
        }
    }

    public static void save(File file, Map<String, Object> data) throws IOException {
        file.getParentFile().mkdirs();
        DumperOptions opt = new DumperOptions();
        opt.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opt.setPrettyFlow(true);
        opt.setIndent(2);
        opt.setIndicatorIndent(2);
        opt.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions().setProcessComments(false)), new Representer(opt), opt);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            yaml.dump(data == null ? new LinkedHashMap<>() : data, writer);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepCopy(Map<?, ?> src) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : src.entrySet()) {
            String key = entry.getKey() == null ? "null" : String.valueOf(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map) out.put(key, deepCopy((Map<?, ?>) value));
            else out.put(key, value);
        }
        return out;
    }
}
