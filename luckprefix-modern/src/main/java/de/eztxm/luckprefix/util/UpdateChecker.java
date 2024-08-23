package de.eztxm.luckprefix.util;

import lombok.Getter;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(URI.create(urlString).toURL().openStream()))) {
            if (reader.lines().toList().isEmpty()) return this.cachedLatestVersion;
            String line = reader.readLine();
            JSONObject jsonObject = new JSONObject(line);
            this.cachedLatestVersion = jsonObject.getString("Latest-Version");
        } catch (IOException ignored) {}
        return this.cachedLatestVersion;
    }
}
