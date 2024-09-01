package de.eztxm.luckprefix.util;

import lombok.Getter;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Getter
public class UpdateChecker {
    private final String currentVersion;
    private String cachedLatestVersion = "N/A";

    public UpdateChecker(String version) {
        this.currentVersion = version;
        this.cachedLatestVersion = getLatestVersion();
    }

    public boolean latestVersion() {
        String latestVersion = getLatestVersion();
        if (latestVersion == null) return true;
        if (this.cachedLatestVersion.equalsIgnoreCase("N/A")) return true;
        return latestVersion.equalsIgnoreCase(currentVersion);
    }

    private String getLatestVersion() {
        String urlString = "https://cdn.eztxm.de/plugin/luckprefix/manifest.json";
        HttpURLConnection connection = null;
        try {
            URL url = URI.create(urlString).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return this.cachedLatestVersion;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                if (response.isEmpty()) {
                    return this.cachedLatestVersion;
                }
                JSONObject jsonObject = new JSONObject(response.toString());
                this.cachedLatestVersion = jsonObject.getString("Latest-Version");
            }
        } catch (IOException e) {
            return this.cachedLatestVersion;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return this.cachedLatestVersion;
    }
}
