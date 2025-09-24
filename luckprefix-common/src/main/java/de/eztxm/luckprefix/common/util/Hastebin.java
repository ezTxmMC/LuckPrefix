package de.eztxm.luckprefix.common.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class Hastebin {
    private Hastebin() {}

    public static String post(String content) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("Hastebin URL");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(6000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(content.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            if (code != 200 && code != 201) return null;

            String resp;
            try (InputStream is = conn.getInputStream()) {
                resp = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            int idx = resp.indexOf("\"key\"");
            if (idx < 0) return null;
            int colon = resp.indexOf(':', idx);
            int quote1 = resp.indexOf('"', colon + 1);
            int quote2 = resp.indexOf('"', quote1 + 1);
            if (quote1 < 0 || quote2 < 0) return null;
            String key = resp.substring(quote1 + 1, quote2).trim();
            if (key.isEmpty()) return null;
            return "https://hastebin URL/" + key;
        } catch (Exception ignored) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}