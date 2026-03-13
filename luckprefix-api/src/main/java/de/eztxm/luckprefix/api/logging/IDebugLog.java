package de.eztxm.luckprefix.api.logging;

import java.nio.file.Path;

public interface IDebugLog {

    void info(String message);
    void warn(String message);
    void error(String message);
    void error(String message, Throwable throwable);
    void debug(String message);
    String tailBytes(int maxBytes);
    Path getPath();

}
