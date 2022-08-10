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
    private static final int BASE_INDEX = 4096;
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
    private static final String ANIMATION_FILE_NAME = "mt_idle.hkx";
    private static final String ANIMATION_FILES_DIRECTORY = "."; //replace with folder that contains .hkx files, takes animations in same folder as executable by default

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
        createAnimationFolders();
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

    private void createAnimationFolders() {
        File folder = new File(ANIMATION_FILES_DIRECTORY);
        File[] listOfFiles = folder.listFiles();

        if(listOfFiles == null) {
            throw new CustomRuntimeException("Could not find any animation files");
        }

        int hkxCount = 0;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile() && isHkxFile(listOfFile)) {
                hkxCount++;
                logger.info("Found hkx file: " + listOfFile.getName());
                createAnimationFolder(String.valueOf(BASE_INDEX + hkxCount - 1), listOfFile, 1.0 / hkxCount);
            }
        }
    }

    private void createAnimationFolder(String folderName, File animationFile, double chance) {
        deleteFolderIfExists(Paths.get(BASE_DIRECTORY + "\\" + folderName));
        Path path = Paths.get(BASE_DIRECTORY + "\\" + folderName + "\\" + FEMALE_FOLDER_NAME);
        Path targetFilePath = Paths.get(BASE_DIRECTORY + "\\" + folderName + "\\" + FEMALE_FOLDER_NAME + "\\" + ANIMATION_FILE_NAME);
        try {
            logger.info("Creating folder " + path.toAbsolutePath());
            Files.createDirectories(path);
            logger.info("Copying animation file " + animationFile.toPath().toAbsolutePath() + " to " + targetFilePath.toAbsolutePath());
            Files.copy(animationFile.toPath(), targetFilePath);
            createConditionsFile(BASE_DIRECTORY + "\\" + folderName, chance);
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create Animation Folder " + path.toAbsolutePath(), e);
        }
    }
    private void createConditionsFile(String path, double chance) {
        try {
            logger.info("Creating conditions file " + path + "\\" + CONDITIONS_FILE_NAME);
            FileOutputStream file = new FileOutputStream(path + "\\" + CONDITIONS_FILE_NAME);
            BufferedOutputStream bufferedWriter = new BufferedOutputStream(file);
            logger.info("Using chance " + trimNumber(chance));
            bufferedWriter.write(getConditionsText(chance).getBytes());
            bufferedWriter.close();
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create Animation Conditions File " + path + CONDITIONS_FILE_NAME, e);
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
