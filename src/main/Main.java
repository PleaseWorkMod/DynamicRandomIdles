package main;

import exception.HandledException;
import exception.UnhandledException;

import java.util.logging.Logger;

public class Main {
    public static final Logger logger = new LoggerBuilder().build();

    public static void main(String[] args) {
        try {
            new Generator().run();
        } catch (HandledException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new UnhandledException(e);
        }
    }
}
