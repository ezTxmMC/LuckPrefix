package de.eztxm.luckprefix.util;

import java.util.ArrayList;
import java.util.List;

public class ConfigUtil {

    public static ConfigManager addDatabaseDefault(String fileName) {
        ConfigManager configManager = new ConfigManager(fileName);
        configManager.addDefault("Database.Enabled", false);
        configManager.addDefault("Database.Type", "SQLITE");
        configManager.addDefault("Database.SQLite.Path", "plugins/LuckPrefix/storage");
        configManager.addDefault("Database.SQLite.FileName", "sqlite.db");
        configManager.addDefault("Database.MariaDB.Host", "localhost");
        configManager.addDefault("Database.MariaDB.Port", 3306);
        configManager.addDefault("Database.MariaDB.Database", "luckprefix");
        configManager.addDefault("Database.MariaDB.User", "luckprefix");
        configManager.addDefault("Database.MariaDB.Password", "");
        configManager.addDefault("Database.MongoDB.Host", "localhost");
        configManager.addDefault("Database.MongoDB.Port", 3306);
        configManager.addDefault("Database.MongoDB.Database", "luckprefix");
        configManager.addDefault("Database.MongoDB.User", "luckprefix");
        configManager.addDefault("Database.MongoDB.Password", "");
        configManager.saveDefaults();
        configManager.setComments("Database", new ArrayList<>(List.of("The both supported types are sqlite and mariadb, otherwise you can still disable it for file-save")));
        configManager.saveComments();
        return configManager;
    }

    public static ConfigManager addGroupsDefault(String fileName) {
        ConfigManager configManager = new ConfigManager(fileName);
        configManager.addDefault("default.Prefix", "<gray>Player");
        configManager.addDefault("default.Suffix", "");
        configManager.addDefault("default.Tabformat", "<prefix> <dark_gray>- <gray><player>");
        configManager.addDefault("default.Chatformat", "<prefix> <dark_gray>- <gray><player> <dark_gray>» <gray><message>");
        configManager.addDefault("default.SortID", 999);
        configManager.addDefault("default.NameColor", "gray");
        configManager.saveDefaults();
        configManager.setComments("default", new ArrayList<>(List.of("The name of the group")));
        configManager.setComments("default.Prefix", new ArrayList<>(List.of(
                "Prefix, suffix, tabformat and chatformat are working with the adventure minimessage format,",
                "but the legacy '&'-codes also work",
                "https://docs.advntr.dev/minimessage/format.html"
        )));
        configManager.setComments("default.SortID", new ArrayList<>(List.of(
                "The sort-id is important for the ordner on the tablist.",
                "The lowest value is on top and the highest at the bottom.",
                "You only can set the sort-id between 1 and 999. More can execute issues."
        )));
        configManager.setComments("default.NameColor", new ArrayList<>(List.of("The color of the name above a player")));
        configManager.saveComments();
        return configManager;
    }
}
