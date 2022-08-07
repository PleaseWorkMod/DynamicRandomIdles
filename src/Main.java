import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.Locale;

public class Main {
    private static final String BASE_DIRECTORY = "Meshes\\Actors\\Character\\Animations\\DynamicAnimationReplacer\\_CustomConditions";
    private static final String DELETE_FOLDER_IF_EXISTS = "Meshes\\Actors\\Character\\Animations\\DynamicAnimationReplacer";
    private static final int BASE_INDEX = 4096;
    private static final String FEMALE_FOLDER_NAME = "female";
    private static final String CONDITIONS_FILE_NAME = "_conditions.txt";
    private static final String CONDITIONS_PC =
            "IsFemale() AND\n" +
            "IsActorBase(\"Skyrim.esm\" | 0X000007) AND\n" +
            "Random(%s)";
    private static final String CONDITIONS_PC_NPC_NOELDERS =
            "IsFemale() AND\n" +
            "NOT IsVoiceType(\"Skyrim.esm\"|0x00013AE2) AND\n" +
            "NOT IsVoiceType(\"Skyrim.esm\"|0x00013AE1) AND\n" +
            "NOT IsVoiceType(\"Skyrim.esm\"|0x00013AD7) AND\n" +
            "NOT IsVoiceType(\"Skyrim.esm\"|0x00013aD6) AND\n" +
            "NOT IsRace(\"Skyrim.esm\"|0x00067cd8) AND\n" +
            "NOT IsChild() AND\n" +
            "Random(%s)";
    private static final String CONDITIONS_PC_NPC_WITH_ELDERS =
            "IsFemale() AND\n" +
            "NOT IsChild() AND\n" +
            "Random(%s)";
    private static final String CONDITIONS = CONDITIONS_PC; //Choose one of the CONDITIONS Strings above
    private static final String ANIMATION_FILE_NAME = "mt_idle.hkx";
    private static final String ANIMATION_FILES_DIRECTORY = "."; //replace with folder that contains .hkx files, takes animations in same folder as executable by default

    public static void main(String[] args) {
        Logger.deleteLogFileIfExists();
        createBaseDirectory();
        createAnimationFolders();
    }

    private static void createBaseDirectory() {
        Path folderToDelete = Paths.get(DELETE_FOLDER_IF_EXISTS);
        if(Files.exists(folderToDelete)) {
            try {
                Logger.debug("Deleting already existing folders.");
                deleteFolder(folderToDelete);
            } catch (IOException e) {
                throw new CustomRuntimeException("Could not delete already existing folders.", e);
            }
        }

        Path path = Paths.get(BASE_DIRECTORY);
        Logger.debug("Creating Directory " + path.toAbsolutePath());
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create Base Directories", e);
        }
    }

    private static void createAnimationFolders() {
        File folder = new File(ANIMATION_FILES_DIRECTORY);
        File[] listOfFiles = folder.listFiles();

        if(listOfFiles == null) {
            throw new CustomRuntimeException("Could not find any animation files");
        }

        int hkxCount = 0;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && isHkxFile(listOfFiles[i])) {
                hkxCount++;
                Logger.debug("Found hkx file: " + listOfFiles[i].getName());
                createAnimationFolder(String.valueOf(BASE_INDEX + hkxCount - 1), listOfFiles[i], 1.0/hkxCount);
            }
        }
    }

    private static void createAnimationFolder(String folderName, File animationFile, double chance) {
        Path path = Paths.get(BASE_DIRECTORY + "\\" + folderName + "\\" + FEMALE_FOLDER_NAME);
        Path targetFilePath = Paths.get(BASE_DIRECTORY + "\\" + folderName + "\\" + FEMALE_FOLDER_NAME + "\\" + ANIMATION_FILE_NAME);
        try {
            Logger.debug("Creating folder " + path.toAbsolutePath());
            Files.createDirectories(path);
            Logger.debug("Copying animation file " + animationFile.toPath().toAbsolutePath() + " to " + targetFilePath.toAbsolutePath());
            String msg = "Copying animation file " + animationFile.toPath().toAbsolutePath() + " to " + targetFilePath.toAbsolutePath();
            Files.copy(animationFile.toPath(), targetFilePath);
            createConditionsFile(BASE_DIRECTORY + "\\" + folderName, chance);
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create Animation Folder " + path.toAbsolutePath(), e);
        }
    }

    private static void createConditionsFile(String path, double chance) {
        try {
            Logger.debug("Creating conditions file " + path + "\\" + CONDITIONS_FILE_NAME);
            FileOutputStream file = new FileOutputStream(path + "\\" + CONDITIONS_FILE_NAME);
            BufferedOutputStream bufferedWriter = new BufferedOutputStream(file);
            Logger.debug("Using chance " + trimNumber(chance));
            bufferedWriter.write(getConditionsText(chance).getBytes());
            bufferedWriter.close();
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create Animation Conditions File " + path + CONDITIONS_FILE_NAME, e);
        }
    }

    private static boolean isHkxFile(File file) {
        return getFileExtension(file.getName()).compareToIgnoreCase("hkx") == 0;
    }

    private static String getFileExtension(String fileName) {
        if(!fileName.contains(".")) {
            return "";
        }
        int dotIndex = fileName.indexOf('.');
        String fileExtension = fileName.substring(dotIndex+1);
        return fileExtension;
    }

    private static String getConditionsText(double chance) {
        return String.format(CONDITIONS, trimNumber(chance));
    }

    private static String trimNumber(double number) {
        DecimalFormatSymbols dotSeparatorSymbols = new DecimalFormatSymbols(Locale.getDefault());
        dotSeparatorSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.0000", dotSeparatorSymbols);
        return df.format(number);
    }

    private static void deleteFolder(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

}
