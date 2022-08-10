package main;

import exception.LoggerBuilderException;

import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerBuilder {
    private static final String LOG_FILE_NAME = "DynamicRandomIdlesLog.txt";
    private static final String FIRST_LINE = "DYNAMIC RANDOM IDLES LOG FILE " +  LOG_FILE_NAME;

    public LoggerBuilder() {
    }

    public Logger build() {
        Logger logger = Logger.getLogger(LOG_FILE_NAME);
        FileHandler fh;

        try {
            // This block configures the logger with handler and formatter
            fh = new FileHandler(LOG_FILE_NAME);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            logger.info(FIRST_LINE);
        } catch (IOException e) {
            throw new LoggerBuilderException("Could not build Logger", e);
        }

        return logger;
    }
}
