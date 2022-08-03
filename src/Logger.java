import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Logger {
    private static String logFileName = "DynamicRandomIdlesLog.txt";

    public static void debug(String msg) {
        try {
            msg += "\n";
            FileOutputStream file = new FileOutputStream(logFileName, true);
            BufferedOutputStream bufferedWriter = new BufferedOutputStream(file);
            bufferedWriter.write(msg.getBytes());
            bufferedWriter.close();
        } catch(IOException e) {
            throw new CustomRuntimeException("Could not write to log file.", e);
        }
    }

    public static void deleteLogFileIfExists() {
        Path logFile = Paths.get(logFileName);
            try {
                Files.deleteIfExists(logFile);
            } catch (IOException e) {
                throw new CustomRuntimeException("Could not delete already existing log file.", e);
            }
    }
}
