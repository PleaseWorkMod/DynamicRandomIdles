package main;

import exception.CustomRuntimeException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.logging.Logger;

public class Conditions {
    private static final String CONDITIONS_DIRECTORY = Generator.CONDITIONS_DIRECTORY;
    private static final String CONDITIONS_FILE_NAME = "_conditions.txt";
    private static final Logger logger = Main.logger;

    private String condition;
    private int conditionBaseIndex;
    private int indexOffset;

    public void setConditionBaseIndex(int conditionBaseIndex) {
        this.conditionBaseIndex = conditionBaseIndex;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void copyToCondition(File animationFile, String targetFileName) {
        Path targetFilePath = Paths.get(CONDITIONS_DIRECTORY, getConditionFolder(), targetFileName);
        try {
            logger.info("Copying animation file " + animationFile.toPath().toAbsolutePath() + " to " + targetFilePath.toAbsolutePath());
            Files.copy(animationFile.toPath(), targetFilePath);
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not copy animation file to " + targetFilePath.toAbsolutePath(), e);
        }
    }

    public void generateNextCondition() {
        indexOffset++;
        createConditionFolder(getConditionFolder());
        createConditionFile(getConditionFolder(), getChance());
    }

    private double getChance() {
        return 1.0 / indexOffset;
    }

    private String getConditionFolder() {
        return String.valueOf(conditionBaseIndex+indexOffset-1);
    }

    private void createConditionFolder(String conditionFolder) {
        Path path = Paths.get(CONDITIONS_DIRECTORY, conditionFolder);
        try {
            logger.info("Creating directory " + path.toAbsolutePath());
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create condition Folder " + path.toAbsolutePath(), e);
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
            throw new CustomRuntimeException("Could not create Animation Conditions File " + path, e);
        }
    }

    private String getConditionText(double chance) {
        try {
            return String.format(condition, trimNumber(chance));
        } catch (IllegalFormatException e) {
            throw new CustomRuntimeException("Could not format condition string", e);
        }
    }

    private String trimNumber(double number) {
        DecimalFormatSymbols dotSeparatorSymbols = new DecimalFormatSymbols(Locale.getDefault());
        dotSeparatorSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.0000", dotSeparatorSymbols);
        return df.format(number);
    }
}
