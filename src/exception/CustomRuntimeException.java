package exception;

import main.Main;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomRuntimeException extends RuntimeException {
    private static final Logger logger = Main.logger;

    public CustomRuntimeException(String msg) {
        super(msg);
        logger.log(Level.SEVERE, msg, this);
    }

    public CustomRuntimeException(String msg, Exception cause) {
        super(msg, cause);
        logger.log(Level.SEVERE, msg, this);
    }
}
