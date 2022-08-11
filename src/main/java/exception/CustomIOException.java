package exception;

public class CustomIOException extends LoggedException {
    public CustomIOException(String msg) {
        super(msg);
    }

    public CustomIOException(String msg, Exception cause) {
        super(msg, cause);
    }
}
