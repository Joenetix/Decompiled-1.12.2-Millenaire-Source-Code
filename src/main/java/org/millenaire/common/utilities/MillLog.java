package org.millenaire.common.utilities;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.forge.Mill;

public class MillLog {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static boolean console = true;

    public static void error(Object source, String message) {
        LOGGER.error("[" + getSourceName(source) + "] " + message);
    }

    public static void warning(Object source, String message) {
        LOGGER.warn("[" + getSourceName(source) + "] " + message);
    }

    public static void major(Object source, String message) {
        if (console)
            LOGGER.info("[MAJOR] [" + getSourceName(source) + "] " + message);
    }

    public static void minor(Object source, String message) {
        if (MillConfigValues.DEV) {
            LOGGER.debug("[MINOR] [" + getSourceName(source) + "] " + message);
        }
    }

    public static void debug(Object source, String message) {
        if (MillConfigValues.DEV) {
            LOGGER.debug("[DEBUG] [" + getSourceName(source) + "] " + message);
        }
    }

    public static void temp(Object source, String message) {
        // Temporary debug messages - always log in DEV mode
        if (MillConfigValues.DEV) {
            LOGGER.info("[TEMP] [" + getSourceName(source) + "] " + message);
        }
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    private static String getSourceName(Object source) {
        return source != null ? source.getClass().getSimpleName() : "null";
    }

    public static void printException(String errorDetail, Throwable e) {
        LOGGER.error(errorDetail, e);
    }

    public static void printException(Throwable e) {
        LOGGER.error("Exception caught by MillLog:", e);
    }

    public static void writeText(String text) {
        // TODO: Write to custom log file
    }

    public static void initLogFileWriter() {
        // TODO
    }

    public static String getLogLevel(int level) {
        switch (level) {
            case 0:
                return "0";
            case 1:
                return "1";
            case 2:
                return "2";
            case 3:
                return "3"; // Map to old string values if needed
            default:
                return "0";
        }
    }

    public static int readLogLevel(String level) {
        try {
            return Integer.parseInt(level);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static class MillenaireException extends Exception {
        private static final long serialVersionUID = 1L;

        public MillenaireException(String string) {
            super(string);
        }
    }
}
