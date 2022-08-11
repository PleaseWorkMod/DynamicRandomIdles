package exception;

import main.Main;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HandledException extends RuntimeException{
    private static final Logger logger = Main.logger;

    public HandledException(String msg) {
        super(msg);
        logger.log(Level.SEVERE, msg, this);
    }

    public HandledException(String msg, Exception cause) {
        super(msg, cause);
        logger.log(Level.SEVERE, msg, this);
    }
}
