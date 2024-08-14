package de.eztxm.luckprefix.util;

import lombok.Getter;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        return latestVersion.equalsIgnoreCase(currentVersion);
    }

    private String getLatestVersion() {
        try {
            String urlString = "https://cdn.eztxm.de/plugin/luckprefix/manifest.json";
            URL url = new URL(urlString);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = reader.readLine();
            JSONObject jsonObject = new JSONObject(line);
            String latestVersion = jsonObject.getString("Latest-Version");
            this.cachedLatestVersion = latestVersion;
            return latestVersion;
        } catch (IOException e) {
            e.fillInStackTrace();
        }
        return "N/A";
    }
}
