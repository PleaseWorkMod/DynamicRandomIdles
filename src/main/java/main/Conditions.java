package main;

import exception.ConfigException;
import exception.CustomIOException;
import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

public class Conditions {
    private static final String CONDITIONS_DIRECTORY = Main.CONDITIONS_DIRECTORY;
    private static final String CONDITIONS_FILE_NAME = Main.CONDITIONS_FILE_NAME;
    private static final String SETS_DIRECTORY = Main.SETS_DIRECTORY;
    private static final Logger logger = Main.logger;

    private final File set;
    private String condition;
    private Integer conditionBaseIndex;
    private Integer indexOffset;

    public Conditions(File set) {
        this.set = set;
        indexOffset = -1;
    }

    public void setConditionBaseIndex(Integer conditionBaseIndex) {
        this.conditionBaseIndex = conditionBaseIndex;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void assertValid() {
        if(condition == null) {
            throw new ConfigException("Set '" + set.getName() + "': Property conditions has to be set");
        }
        if(conditionBaseIndex == null) {
            throw new ConfigException("Set '" + set.getName() + "': Property conditionBaseIndex has to be set");
        }
    }

    public void copyToCondition(File file, String targetFileName) {
        Path targetFilePath = Paths.get(CONDITIONS_DIRECTORY, getConditionFolder(), targetFileName);
        try {
            logger.info("Copying animation file " + file.toPath().toAbsolutePath() + " to " + targetFilePath.toAbsolutePath());
            if(file.isFile()) {
                FileUtils.copyFile(file, targetFilePath.toFile());
            } else {
                throw new CustomIOException("Cannot copy a directory");
            }
        } catch (IOException e) {
            throw new CustomIOException("Could not copy animation file to " + targetFilePath.toAbsolutePath(), e);
        }
    }

    public void generateNextCondition(File origin) {
        indexOffset++;
        createConditionFolder(getConditionFolder());
        createConditionFile(getConditionFolder(), getChance());
        createOriginFile(getConditionFolder(), origin);
    }

    private void createConditionFolder(String conditionFolder) {
        Path path = Paths.get(CONDITIONS_DIRECTORY, conditionFolder);
        try {
            logger.info("Creating directory " + path.toAbsolutePath());
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new CustomIOException("Could not create condition Folder " + path.toAbsolutePath(), e);
        }
    }

    private void createOriginFile(String conditionFolder, File origin) {
        Path relativeOrigin = Paths.get(SETS_DIRECTORY).relativize(origin.toPath());
        String[] originArray = StreamSupport
                .stream(relativeOrigin.spliterator(), false)
                .map(Path::toString)
                .toArray(String[]::new);
        String originFile = String.join(".", originArray) + ".txt";
        Path path = Paths.get(CONDITIONS_DIRECTORY, conditionFolder, originFile);
        try {
            logger.info("Creating origin file " + path.toAbsolutePath());
            Files.createFile(path);
        } catch (IOException e) {
            throw new CustomIOException("Could not create origin file " + path.toAbsolutePath(), e);
        }
    }

    private void createConditionFile(String conditionFolder, double chance) {
        Path path = Paths.get(CONDITIONS_DIRECTORY, conditionFolder, CONDITIONS_FILE_NAME);
        try {
            logger.info("Creating conditions file " + path);
            FileOutputStream file = new FileOutputStream(path.toString());
            BufferedOutputStream bufferedWriter = new BufferedOutputStream(file);
            logger.info("Using chance " + trimNumber(chance));
            bufferedWriter.write(getConditionText(chance).getBytes());
            bufferedWriter.close();
        } catch (IOException e) {
            throw new CustomIOException("Could not create Animation Conditions File " + path, e);
        }
    }

    private String getConditionText(double chance) {
        try {
            return String.format(condition, trimNumber(chance));
        } catch (IllegalFormatException e) {
            throw new CustomIOException("Could not format condition string", e);
        }
    }

    private double getChance() {
        return 1.0 / (indexOffset+1);
    }

    private String getConditionFolder() {
        return String.valueOf(conditionBaseIndex+indexOffset);
    }

    private String trimNumber(double number) {
        DecimalFormatSymbols dotSeparatorSymbols = new DecimalFormatSymbols(Locale.getDefault());
        dotSeparatorSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.0000", dotSeparatorSymbols);
        return df.format(number);
    }
}
