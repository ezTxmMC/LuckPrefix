package de.eztxm.luckprefix.util;

import de.eztxm.luckprefix.LuckPrefix;
import de.eztxm.object.ObjectConverter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class FileManager {
    private final File file;
    private final YamlConfiguration configuration;

    @SneakyThrows
    public FileManager(String fileName) {
        this.file = new File("plugins/" + LuckPrefix.getInstance().getDescription().getName() + "/" + fileName);
        if (!this.file.exists()) {
            this.file.createNewFile();
        }
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public void addDefault(String path, Object value) {
        this.configuration.addDefault(path, value);
    }

    public void setComments(String path, List<String> comments) {
        this.configuration.setComments(path, comments);
    }

    public void saveDefaults() {
        this.configuration.options().copyDefaults(true);
        this.saveConfiguration();
    }

    public void saveComments() {
        this.configuration.options().parseComments(true);
        this.saveConfiguration();
    }

    @SneakyThrows
    public void saveConfiguration() {
        this.configuration.save(file);
    }

    public ObjectConverter getValue(String path) {
        Object value = this.configuration.get(path);
        if (value == null) {
            return null;
        }
        return new ObjectConverter(value);
    }

    public boolean contains(String path) {
        return this.configuration.contains(path);
    }
}
