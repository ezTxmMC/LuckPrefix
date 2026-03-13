package de.eztxm.luckprefix.common.logging;

import de.eztxm.luckprefix.api.logging.IDebugLog;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public final class DebugLog implements IDebugLog {
    private final Path logFilePath;
    private final long rotateBytes;
    private final ReentrantLock lock = new ReentrantLock();

    public DebugLog(Path logFilePath, long rotateBytes) {
        this.logFilePath = Objects.requireNonNull(logFilePath, "logFilePath");
        this.rotateBytes = Math.max(256 * 1024, rotateBytes);
    }

    @Override
    public void info(String msg) {
        write("INFO", msg, null);
    }

    @Override
    public void warn(String msg) {
        write("WARN", msg, null);
    }

    @Override
    public void error(String msg) {
        write("ERROR", msg, null);
    }

    @Override
    public void error(String msg, Throwable t) {
        write("ERROR", msg, t);
    }

    @Override
    public void debug(String msg) {
        write("DEBUG", msg, null);
    }

    private void write(String level, String msg, Throwable t) {
        lock.lock();
        try {
            File file = logFilePath.toFile();
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();

            if (file.exists() && file.length() > rotateBytes) {
                String ts = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
                File rotated = new File(file.getParentFile(), file.getName() + "." + ts + ".1");

                if (!file.renameTo(rotated)) {

                    try (OutputStream os = new FileOutputStream(file, true)) {
                        os.write(("\n--- ROTATE FAILED @ " + ts + " ---\n").getBytes(StandardCharsets.UTF_8));
                    }
                }
            }

            try (OutputStream os = new FileOutputStream(file, true);
                 OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                 BufferedWriter bw = new BufferedWriter(osw)) {
                String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                bw.write(ts + " [" + level + "] " + msg);
                bw.newLine();
                if (t != null) {
                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));
                    bw.write(sw.toString());
                }
            }
        } catch (IOException ignored) {
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String tailBytes(int maxBytes) {
        lock.lock();
        try {
            File file = logFilePath.toFile();
            if (!file.exists()) return "";
            long len = file.length();
            long start = Math.max(0, len - maxBytes);
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(start);
                byte[] buf = new byte[(int) (len - start)];
                raf.readFully(buf);
                return new String(buf, StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
            return "";
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Path getPath() {
        return logFilePath;
    }
}