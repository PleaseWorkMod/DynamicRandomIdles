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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Generator implements Runnable {
    private static final String BASE_DIRECTORY = "Meshes\\Actors\\Character\\Animations\\DynamicAnimationReplacer\\_CustomConditions";
    private static final int BASE_IDLE_INDEX = 4096;
    private static final int BASE_MOVEMENT_INDEX = 256;
    private static final String FEMALE_FOLDER_NAME = "female";
    private static final String CONDITIONS_FILE_NAME = "_conditions.txt";
    private static final String CONDITIONS_PC =
            "IsFemale() AND\n" +
                    "IsActorBase(\"Skyrim.esm\" | 0X000007) AND\n" +
                    "Random(%s)";
    private static final String CONDITIONS_PC_NPC =
            "IsFemale() AND\n" +
                    "NOT IsVoiceType(\"Skyrim.esm\"|0x00013AE2) AND\n" +
                    "NOT IsVoiceType(\"Skyrim.esm\"|0x00013AE1) AND\n" +
                    "NOT IsVoiceType(\"Skyrim.esm\"|0x00013AD7) AND\n" +
                    "NOT IsVoiceType(\"Skyrim.esm\"|0x00013aD6) AND\n" +
                    "NOT IsRace(\"Skyrim.esm\"|0x00067cd8) AND\n" +
                    "NOT IsChild() AND\n" +
                    "Random(%s)";
    private static final String CONDITIONS_PC_NPC_ELDERS =
            "IsFemale() AND\n" +
                    "NOT IsChild() AND\n" +
                    "Random(%s)";
    private static final String IDLE_FILE_NAME = "mt_idle.hkx";
    private static final String DYNAMIC_IDLES_DIRECTORY = "dynamic_idles"; //replace with folder that contains .hkx files, takes animations in same folder as executable by default
    private static final String DYNAMIC_SETS_DIRECTORY = "dynamic_sets"; //replace with folder that contains .hkx files, takes animations in same folder as executable by default

    private static final Logger logger = Main.logger;

    private final ConditionType conditionType;
    private final String CONDITIONS; //Choose one of the CONDITIONS Strings above

    public Generator(ConditionType conditionType) {
        this.conditionType = conditionType;
        switch (conditionType) {
            case PC:
                CONDITIONS = CONDITIONS_PC;
                break;
            case PC_NPC:
                CONDITIONS = CONDITIONS_PC_NPC;
                break;
            case PC_NPC_ELDERS:
                CONDITIONS = CONDITIONS_PC_NPC_ELDERS;
                break;
            default:
                throw new CustomRuntimeException("Condition Type " + conditionType + " unknown");
        }
        logger.info("SET CONDITION TYPE AS " + this.conditionType);
    }

    @Override
    public void run() {
        createBaseDirectory();
        createFiles();
    }

    private void createBaseDirectory() {
        Path path = Paths.get(BASE_DIRECTORY);
        logger.info("Found existing base directory " + path.toAbsolutePath());
        if(!Files.exists(path)) {
            logger.info("Creating Directory " + path.toAbsolutePath());
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new CustomRuntimeException("Could not create Base Directories", e);
            }
        }
    }

    private void createFiles() {
        if(Files.exists(Paths.get(DYNAMIC_SETS_DIRECTORY))) {
            createSetConditions();
        } else {
            logger.info("Movement folder " + DYNAMIC_SETS_DIRECTORY + " not found.");
        }

        if(Files.exists(Paths.get(DYNAMIC_IDLES_DIRECTORY))) {
            createIdleConditions();
        } else {
            logger.info("Idles folder " + DYNAMIC_SETS_DIRECTORY + " not found.");
        }
    }

    private void createIdleConditions() {
        File folder = new File(DYNAMIC_IDLES_DIRECTORY);
        File[] listOfFiles = folder.listFiles();

        if(listOfFiles == null) {
            throw new CustomRuntimeException("Could not get list of idle animation files");
        }

        int animationCount = 0;
        for (File animationFile : listOfFiles) {
            if (animationFile.isFile() && isHkxFile(animationFile)) {
                logger.info("Found idle file: " + animationFile.getName());
                String conditionFolder = String.valueOf(BASE_IDLE_INDEX + animationCount);
                animationCount++;
                double chance = 1.0 / animationCount;
                createConditionFolder(conditionFolder);
                createConditionFile(conditionFolder, chance);
                copyToConditionFolder(animationFile, conditionFolder, IDLE_FILE_NAME);
            }
        }
    }

    private void createSetConditions() {
        File folder = new File(DYNAMIC_SETS_DIRECTORY);
        File[] listOfFolders = folder.listFiles();

        if(listOfFolders == null) {
            throw new CustomRuntimeException("Could not get list of set animation folders");
        }

        int folderCount = 0;
        for (File movementFolder : listOfFolders) {
            if(movementFolder.isDirectory()) {
                logger.info("Found set folder: " + movementFolder.getName());

                String conditionFolder = String.valueOf(BASE_MOVEMENT_INDEX + folderCount);
                folderCount++;
                double chance = 1.0 / folderCount;
                createConditionFolder(conditionFolder);
                createConditionFile(conditionFolder, chance);

                File[] listOfAnimations = movementFolder.listFiles();
                if(listOfAnimations == null) {
                    throw new CustomRuntimeException("Could not get list of set animation files");
                }
                for (File animationFile : listOfAnimations) {
                    if (animationFile.isFile() && isHkxFile(animationFile)) {
                        logger.info("Found hkx file: " + animationFile.getName());
                        copyToConditionFolder(animationFile, conditionFolder, animationFile.getName());
                    }
                }
            }
        }
    }

    private void createConditionFolder(String conditionFolder) {
        deleteFolderIfExists(Paths.get(BASE_DIRECTORY + "\\" + conditionFolder));
        Path path = Paths.get(BASE_DIRECTORY + "\\" + conditionFolder + "\\" + FEMALE_FOLDER_NAME);
        try {
            logger.info("Creating directory " + path.toAbsolutePath());
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create condition Folder " + path.toAbsolutePath(), e);
        }
    }

    private void copyToConditionFolder(File animationFile, String conditionFolder, String targetFileName) {
        Path targetFilePath = Paths.get(BASE_DIRECTORY + "\\" + conditionFolder + "\\" + FEMALE_FOLDER_NAME + "\\" + targetFileName);
        try {
            logger.info("Copying animation file " + animationFile.toPath().toAbsolutePath() + " to " + targetFilePath.toAbsolutePath());
            Files.copy(animationFile.toPath(), targetFilePath);
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not copy animation file to " + targetFilePath.toAbsolutePath(), e);
        }
    }

    private void createConditionFile(String conditionFolder, double chance) {
        Path path = Paths.get(BASE_DIRECTORY, conditionFolder, CONDITIONS_FILE_NAME);
        try {
            logger.info("Creating conditions file " + path);
            FileOutputStream file = new FileOutputStream(path.toString());
            BufferedOutputStream bufferedWriter = new BufferedOutputStream(file);
            logger.info("Using chance " + trimNumber(chance));
            bufferedWriter.write(getConditionsText(chance).getBytes());
            bufferedWriter.close();
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create Animation Conditions File " + path, e);
        }
    }

    private boolean isHkxFile(File file) {
        return getFileExtension(file.getName()).compareToIgnoreCase("hkx") == 0;
    }

    private String getFileExtension(String fileName) {
        if(!fileName.contains(".")) {
            return "";
        }
        int dotIndex = fileName.indexOf('.');
        String fileExtension = fileName.substring(dotIndex+1);
        return fileExtension;
    }

    private String getConditionsText(double chance) {
        return String.format(CONDITIONS, trimNumber(chance));
    }

    private String trimNumber(double number) {
        DecimalFormatSymbols dotSeparatorSymbols = new DecimalFormatSymbols(Locale.getDefault());
        dotSeparatorSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.0000", dotSeparatorSymbols);
        return df.format(number);
    }

    private void deleteFolderIfExists(Path path) {
        if (Files.exists(path)) {
            deleteFolder(path);
        }
    }

    private void deleteFolder(Path path) {
        logger.info("Deleting folder " + path);
        try (Stream<Path> pathStream = Files
                .walk(path)
                .sorted(Comparator.reverseOrder())) {

            List<Path> pathList = pathStream
                    .collect(Collectors.toList());

            for (Path p : pathList) {
                if (!Files.deleteIfExists(p)) {
                    throw new CustomRuntimeException("Could not delete folder " + path);
                }
            }
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not delete path " + path.toAbsolutePath(), e);
        }

    }
}
