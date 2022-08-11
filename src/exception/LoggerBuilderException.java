package exception;

public class LoggerBuilderException extends HandledException {
    public LoggerBuilderException(String msg, Exception cause) {
        super(msg, cause);
    }
}
