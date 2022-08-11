package main;

import exception.CustomRuntimeException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Generator implements Runnable {
    public static final String CONDITIONS_DIRECTORY = Paths.get("Meshes", "Actors", "Character", "Animations", "DynamicAnimationReplacer", "_CustomConditions").toString();;
    public static final String SETS_DIRECTORY = "sets";
    private static final Logger logger = Main.logger;

    @Override
    public void run() {
        createBaseDirectory();
        File[] sets = getFileList(new File(SETS_DIRECTORY));
        iterateSets(sets);
    }

    private void createBaseDirectory() {
        Path path = Paths.get(CONDITIONS_DIRECTORY);
        deleteFolderIfExists(path);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not create Base Directories", e);
        }
    }

    private void iterateSets(File[] sets) {
        for (File set : sets) {
            if(set.isDirectory()) {
                logger.info("Found set: " + set.getName());
                Config config = new Config();
                config.loadFor(set.toPath());
                File[] packs = getFileList(set);
                iteratePacks(packs, config);
            }
        }
    }

    private void iteratePacks(File[] packs, Config config) {
        for (File pack : packs) {
            if(pack.isDirectory()) {
                logger.info("Found pack within set: " + pack.getName());
                config.getConditions().generateNextCondition();
                File[] animations = getFileList(pack);
                iterateAnimations(animations, config);
            }
            if(pack.isFile() && isHkxFile(pack)) {
                logger.info("Found animation within set: " + pack.getName());
                config.getConditions().generateNextCondition();
                config.getConditions().copyToCondition(pack, config.getTargetFileName());
            }
        }
    }

    private void iterateAnimations(File[] animations, Config config) {
        for (File animation : animations) {
            if (animation.isFile() && isHkxFile(animation)) {
                logger.info("Found animation file within pack: " + animation.getName());
                config.getConditions().copyToCondition(animation, animation.getName());
            }
        }
    }

    private File[] getFileList(File folder) {
        File[] list = folder.listFiles();
        if(list == null) {
            throw new CustomRuntimeException("Could not get list of files in " + folder.getAbsolutePath());
        }
        return list;
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
}
