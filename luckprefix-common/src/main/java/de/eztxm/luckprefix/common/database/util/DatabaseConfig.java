package de.eztxm.luckprefix.common.database.util;

import java.util.HashMap;
import java.util.Map;

public class DatabaseConfig {

    private final DatabaseType type;
    private final String connectionString;
    private final String username;
    private final String password;
    private final Map<String, Object> properties = new HashMap<>();

    public static class Builder {
        private DatabaseType type;
        private String connectionString;
        private String username;
        private String password;
        private final Map<String, Object> properties = new HashMap<>();

        public Builder type(DatabaseType type) {
            this.type = type;
            return this;
        }

        public Builder connectionString(String connectionString) {
            this.connectionString = connectionString;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder property(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        public DatabaseConfig build() {
            return new DatabaseConfig(this);
        }
    }

    private DatabaseConfig(Builder builder) {
        this.type = builder.type;
        this.connectionString = builder.connectionString;
        this.username = builder.username;
        this.password = builder.password;
        this.properties.putAll(builder.properties);
    }

    public DatabaseType getType() {
        return type;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
