import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.Locale;

public class Main {
    private static String baseDirectory = "Meshes\\Actors\\Character\\Animations\\DynamicAnimationReplacer\\_CustomConditions";
    private static String deleteFolderIfExists = "Meshes\\Actors\\Character\\Animations\\DynamicAnimationReplacer";
    private static int baseIndex = 4096;
    private static String femaleFolderName = "female";
    private static String conditionsFileName = "_conditions.txt";
    private static String conditionsFileText = "IsFemale() AND\n"
                    + "IsActorBase(\"Skyrim.esm\" | 0X000007) AND\n"
                    + "Random(%s)";
    private static String animationFileName = "mt_idle.hkx";
    private static String animationFilesDirectory = "."; //replace with folder that contains .hkx files, takes animations in same folder as executable by default

    public static void main(String[] args) {
        Logger.deleteLogFileIfExists();
        createBaseDirectory();
        createAnimationFolders();
    }

    private static void createBaseDirectory() {
        Path folderToDelete = Paths.get(deleteFolderIfExists);
        if(Files.exists(folderToDelete)) {
            try {
                Logger.debug("Deleting already existing folders.");
                deleteFolder(folderToDelete);
            } catch (IOException e) {
                throw new CustomRuntimeException("Could not delete already existing folders.", e);
            }
        }

        Path path = Paths.get(baseDirectory);
        Logger.debug("Creating Directory " + path.toAbsolutePath());
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create Base Directories", e);
        }
    }

    private static void createAnimationFolders() {
        File folder = new File(animationFilesDirectory);
        File[] listOfFiles = folder.listFiles();

        if(listOfFiles == null) {
            throw new CustomRuntimeException("Could not find any animation files");
        }

        int hkxCount = 0;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && isHkxFile(listOfFiles[i])) {
                hkxCount++;
                Logger.debug("Found hkx file: " + listOfFiles[i].getName());
                createAnimationFolder(String.valueOf(baseIndex + hkxCount - 1), listOfFiles[i], 1.0/hkxCount);
            }
        }
    }

    private static void createAnimationFolder(String folderName, File animationFile, double chance) {
        Path path = Paths.get(baseDirectory + "\\" + folderName + "\\" + femaleFolderName);
        Path targetFilePath = Paths.get(baseDirectory + "\\" + folderName + "\\" + femaleFolderName + "\\" + animationFileName);
        try {
            Logger.debug("Creating folder " + path.toAbsolutePath());
            Files.createDirectories(path);
            Logger.debug("Copying animation file " + animationFile.toPath().toAbsolutePath() + " to " + targetFilePath.toAbsolutePath());
            String msg = "Copying animation file " + animationFile.toPath().toAbsolutePath() + " to " + targetFilePath.toAbsolutePath();
            Files.copy(animationFile.toPath(), targetFilePath);
            createConditionsFile(baseDirectory + "\\" + folderName, chance);
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create Animation Folder " + path.toAbsolutePath(), e);
        }
    }

    private static void createConditionsFile(String path, double chance) {
        try {
            Logger.debug("Creating conditions file " + path + "\\" + conditionsFileName);
            FileOutputStream file = new FileOutputStream(path + "\\" + conditionsFileName);
            BufferedOutputStream bufferedWriter = new BufferedOutputStream(file);
            Logger.debug("Using chance " + trimNumber(chance));
            bufferedWriter.write(getConditionsText(chance).getBytes());
            bufferedWriter.close();
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create Animation Conditions File " + path + conditionsFileName, e);
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
        return String.format(conditionsFileText, trimNumber(chance));
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
