public class CustomRuntimeException extends RuntimeException {
    public CustomRuntimeException(String msg) {
        super(msg);
        Logger.debug(msg);
    }

    public CustomRuntimeException(String msg, Exception cause) {
        super(msg, cause);
        Logger.debug(msg);
    }
}
