package exception;

public class CustomRuntimeException extends HandledException {
    public CustomRuntimeException(String msg) {
        super(msg);
    }

    public CustomRuntimeException(String msg, Exception cause) {
        super(msg, cause);
    }
}
