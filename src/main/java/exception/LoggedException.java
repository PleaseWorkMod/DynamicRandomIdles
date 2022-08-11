package exception;

import main.Main;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggedException extends RuntimeException{
    private static final Logger logger = Main.logger;

    public LoggedException(String msg) {
        super(msg);
        logger.log(Level.SEVERE, msg, this);
    }

    public LoggedException(String msg, Exception cause) {
        super(msg, cause);
        logger.log(Level.SEVERE, msg, this);
    }
}
