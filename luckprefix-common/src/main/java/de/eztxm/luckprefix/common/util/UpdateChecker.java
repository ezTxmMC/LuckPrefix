package de.eztxm.luckprefix.common.util;

import de.eztxm.luckprefix.api.logging.IDebugLog;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class UpdateChecker implements AutoCloseable {
    private final String updateChannel;
    private final String currentVersion;
    private final IDebugLog debugLog;
    private final ExecutorService executorService;
    private final AtomicBoolean refreshInFlight = new AtomicBoolean(false);
    private volatile JSONObject manifest;
    private volatile String cachedLatestVersion = "N/A";
    private volatile boolean forceUpdate;

    public UpdateChecker(String updateChannel, String version, IDebugLog debugLog) {
        this.updateChannel = updateChannel;
        this.currentVersion = version;
        this.debugLog = debugLog;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "LuckPrefix-UpdateChecker");
            thread.setDaemon(true);
            return thread;
        });
        this.debugLog.debug("Initializing UpdateChecker with channel: " + updateChannel + ", version: " + version);
    }

    public boolean isLatestVersion(boolean development) {
        this.debugLog.debug("Checking if version is latest - development mode: " + development);
        if (this.forceUpdate || this.updateChannel.equalsIgnoreCase("snapshot")) {
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

    public CompletableFuture<Boolean> refreshAsync(boolean development) {
        if (!refreshInFlight.compareAndSet(false, true)) {
            return CompletableFuture.completedFuture(isLatestVersion(development));
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                refreshCache();
                return isLatestVersion(development);
            } finally {
                refreshInFlight.set(false);
            }
        }, executorService);
    }

    @Override
    public void close() {
        executorService.shutdownNow();
    }

    private void refreshCache() {
        JSONObject fetchedManifest = fetchManifest();
        this.manifest = fetchedManifest;
        this.forceUpdate = resolveForceUpdate(fetchedManifest);
        this.cachedLatestVersion = resolveLatestVersion(fetchedManifest);
    }

    private JSONObject fetchManifest() {
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
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                if (response.isEmpty()) {
                    this.debugLog.warn("Manifest response is empty");
                    return null;
                }
                JSONObject parsedManifest = new JSONObject(response.toString());
                this.debugLog.debug("Successfully parsed manifest JSON");
                return parsedManifest;
            }
        } catch (IOException e) {
            this.debugLog.error("Error fetching manifest", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String resolveLatestVersion(JSONObject currentManifest) {
        this.debugLog.debug("Fetching latest version for channel: " + updateChannel);
        
        if (currentManifest == null) {
            this.debugLog.debug("Manifest is null, skipping version fetch");
            return "N/A";
        }
        
        JSONObject latestVersion = currentManifest.optJSONObject("Latest-Version");
        if (latestVersion == null) {
            this.debugLog.warn("Latest-Version object not found in manifest");
            return "N/A";
        }
        
        String capitalizedUpdateChannel = this.updateChannel.substring(0, 1).toUpperCase() + this.updateChannel.substring(1).toLowerCase();
        this.debugLog.debug("Looking for version in channel: " + capitalizedUpdateChannel);
        
        String latestVersionByChannel = latestVersion.optString(capitalizedUpdateChannel, "N/A");
        if (latestVersionByChannel == null || latestVersionByChannel.isBlank()) {
            this.debugLog.warn("No version found for channel: " + capitalizedUpdateChannel);
            return "N/A";
        }
        
        this.debugLog.debug("Latest version for channel " + capitalizedUpdateChannel + ": " + latestVersionByChannel);
        return latestVersionByChannel;
    }

    private boolean resolveForceUpdate(JSONObject currentManifest) {
        this.debugLog.debug("Checking for force update");
        
        if (currentManifest == null) {
            this.debugLog.debug("Manifest is null, no force update");
            return false;
        }
        
        boolean forceUpdate = currentManifest.optBoolean("Force-Update", false);
        this.debugLog.debug("Force-Update flag: " + forceUpdate);
        
        if (!forceUpdate) {
            return false;
        }
        
        JSONArray forceUpdateVersions = currentManifest.optJSONArray("Force-Update-Versions");
        if (forceUpdateVersions == null) {
            this.debugLog.warn("Force-Update is enabled but Force-Update-Versions is missing");
            return false;
        }
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
