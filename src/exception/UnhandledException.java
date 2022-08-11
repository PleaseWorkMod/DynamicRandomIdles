package exception;

import main.Main;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UnhandledException extends RuntimeException {
    private static final Logger logger = Main.logger;

    public UnhandledException(Throwable cause) {
        super(cause);
        logger.log(Level.SEVERE, "Caught unhandled exception", this);
    }
}
