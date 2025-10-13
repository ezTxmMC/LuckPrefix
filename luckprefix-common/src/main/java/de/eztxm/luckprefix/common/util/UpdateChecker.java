package de.eztxm.luckprefix.common.util;

import de.eztxm.luckprefix.api.logging.DebugLog;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Getter
public class UpdateChecker {
    private final String updateChannel;
    private final String currentVersion;
    private final DebugLog debugLog;
    private JSONObject manifest;
    private String cachedLatestVersion = "N/A";

    public UpdateChecker(String updateChannel, String version, DebugLog debugLog) {
        this.updateChannel = updateChannel;
        this.currentVersion = version;
        this.debugLog = debugLog;
        this.debugLog.debug("Initializing UpdateChecker with channel: " + updateChannel + ", version: " + version);
        this.fetchManifest();
        this.fetchLatestVersion();
    }

    public boolean isLatestVersion(boolean development) {
        this.debugLog.debug("Checking if version is latest - development mode: " + development);
        this.fetchManifest();
        this.fetchLatestVersion();
        if (this.isForceUpdate() || this.updateChannel.equalsIgnoreCase("snapshot")) {
            this.debugLog.debug("Force update detected, returning false");
            return false;
        }
        if (development) {
            this.debugLog.debug("Development mode enabled, skipping version check");
            return true;
        }
        if (this.cachedLatestVersion.equalsIgnoreCase("N/A")) {
            this.debugLog.debug("Latest version is N/A, returning true");
            return true;
        }
        boolean isLatest = this.cachedLatestVersion.equalsIgnoreCase(currentVersion);
        this.debugLog.debug("Version comparison - current: " + currentVersion + ", latest: " + cachedLatestVersion + ", isLatest: " + isLatest);
        return isLatest;
    }

    private void fetchManifest() {
        String urlString = "https://cdn.eztxm.de/addon/luckprefix/manifest.json";
        this.debugLog.debug("Fetching manifest from: " + urlString);
        HttpURLConnection connection = null;
        try {
            URL url = URI.create(urlString).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            this.debugLog.debug("Manifest fetch response code: " + responseCode);
            if (responseCode != 200) {
                this.debugLog.warn("Failed to fetch manifest, response code: " + responseCode);
                this.manifest = null;
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                if (response.isEmpty()) {
                    this.debugLog.warn("Manifest response is empty");
                    this.manifest = null;
                    return;
                }
                this.manifest = new JSONObject(response.toString());
                this.debugLog.debug("Successfully parsed manifest JSON");
            }
        } catch (IOException e) {
            this.debugLog.error("Error fetching manifest", e);
            this.manifest = null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void fetchLatestVersion() {
        this.debugLog.debug("Fetching latest version for channel: " + updateChannel);
        
        if (this.manifest == null) {
            this.debugLog.debug("Manifest is null, skipping version fetch");
            return;
        }
        
        JSONObject latestVersion = this.manifest.getJSONObject("Latest-Version");
        if (latestVersion == null) {
            this.debugLog.warn("Latest-Version object not found in manifest");
            return;
        }
        
        String capitalizedUpdateChannel = this.updateChannel.substring(0, 1).toUpperCase() + this.updateChannel.substring(1).toLowerCase();
        this.debugLog.debug("Looking for version in channel: " + capitalizedUpdateChannel);
        
        String latestVersionByChannel = latestVersion.getString(capitalizedUpdateChannel);
        if (latestVersionByChannel == null) {
            this.debugLog.warn("No version found for channel: " + capitalizedUpdateChannel);
            return;
        }
        
        this.cachedLatestVersion = latestVersionByChannel;
        this.debugLog.debug("Latest version for channel " + capitalizedUpdateChannel + ": " + latestVersionByChannel);
    }

    private boolean isForceUpdate() {
        this.debugLog.debug("Checking for force update");
        
        if (this.manifest == null) {
            this.debugLog.debug("Manifest is null, no force update");
            return false;
        }
        
        boolean forceUpdate = this.manifest.getBoolean("Force-Update");
        this.debugLog.debug("Force-Update flag: " + forceUpdate);
        
        if (!forceUpdate) {
            return false;
        }
        
        JSONArray forceUpdateVersions = this.manifest.getJSONArray("Force-Update-Versions");
        this.debugLog.debug("Checking " + forceUpdateVersions.length() + " force update versions");
        
        for (int i = 0; i < forceUpdateVersions.length(); i++) {
            String version = forceUpdateVersions.getString(i);
            this.debugLog.debug("Checking force update version: " + version + " against current: " + this.currentVersion);
            
            if (version.equals(this.currentVersion)) {
                this.debugLog.debug("Current version matches force update version: " + version);
                return true;
            }
        }
        
        this.debugLog.debug("Current version not in force update list");
        return false;
    }
}
