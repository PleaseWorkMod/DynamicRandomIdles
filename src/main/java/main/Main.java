package main;

import exception.LoggedException;
import exception.LoggerBuilderException;

import java.nio.file.Paths;
import java.util.logging.Logger;

public class Main {
    public static final String SETS_DIRECTORY = "sets";
    public static final String CONDITIONS_DIRECTORY = Paths.get("Meshes", "Actors", "Character", "Animations", "DynamicAnimationReplacer", "_CustomConditions").toString();;
    public static final String CONFIG_NAME = "_config.properties";
    public static final String CONDITIONS_FILE_NAME = "_conditions.txt";
    public static final Logger logger = new LoggerBuilder().build();

    public static void main(String[] args) {
        try {
            new Generator().run();
        } catch (LoggedException | LoggerBuilderException e) {
            //Logged exception is just rethrown
            //logger exception cannot be logged, so also rethrow it
            throw e;
        } catch (RuntimeException e) {
            throw new LoggedException("Caught unhandled exception", e);
        }
    }
}
