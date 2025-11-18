package org.alvindimas05.lagassist.utils;

import java.util.logging.Logger;

public class CustomLogger {
    private static final Logger logger = Logger.getLogger("LagAssist");

    public static void info(String message) {
        logger.info(message);
    }

    public static void warning(String message) {
        logger.warning(message);
    }

    public static void severe(String message) {
        logger.severe(message);
    }

    public static void error(String message) {
        logger.severe(message);
    }
}
