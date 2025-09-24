package de.eztxm.luckprefix.common.util;

import org.yaml.snakeyaml.DumperOptions;
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
        try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Yaml yaml = new Yaml(new SafeConstructor());
            Object o = yaml.load(r);
            if (o instanceof Map) return deepCopy((Map<?, ?>) o);
            return new LinkedHashMap<>();
        }
    }

    public static Map<String, Object> loadFromString(String yamlText) throws IOException {
        File tmp = File.createTempFile("lp-default", ".yml");
        try {
            save(tmp, null);
            try (Writer w = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
                w.write(yamlText == null ? "" : yamlText);
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
        Yaml yaml = new Yaml(new SafeConstructor(), new Representer(opt), opt);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            yaml.dump(data == null ? new LinkedHashMap<>() : data, w);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepCopy(Map<?, ?> src) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : src.entrySet()) {
            String k = e.getKey() == null ? "null" : String.valueOf(e.getKey());
            Object v = e.getValue();
            if (v instanceof Map) out.put(k, deepCopy((Map<?, ?>) v));
            else out.put(k, v);
        }
        return out;
    }
}
