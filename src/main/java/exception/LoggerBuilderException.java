package exception;

public class LoggerBuilderException extends RuntimeException {
    public LoggerBuilderException(String msg, Exception cause) {
        super(msg, cause);
    }
}
