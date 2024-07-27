package de.eztxm.luckprefix.util;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker {
    private final String version;

    public UpdateChecker(String version) {
        this.version = version;
    }

    public boolean latestVersion() {
        String latestVersion = getLatestVersion();
        if (latestVersion == null) return true;
        return latestVersion.equalsIgnoreCase(version);
    }

    public String getLatestVersion() {
        try {
            String urlString = "https://cdn.eztxm.de/plugin/luckprefix/manifest.json";
            URL url = new URL(urlString);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = reader.readLine();
            JSONObject jsonObject = new JSONObject(line);
            return jsonObject.getString("Latest-Version");
        } catch (IOException e) {
            e.fillInStackTrace();
        }
        return null;
    }
}
