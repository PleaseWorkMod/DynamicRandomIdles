package exception;

public class ConfigException extends LoggedException{
    public ConfigException(String msg) {
        super(msg);
    }

    public ConfigException(String msg, Exception cause) {
        super(msg, cause);
    }
}
