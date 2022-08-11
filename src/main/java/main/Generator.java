package main;

import exception.CustomIOException;

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
    private static final String SETS_DIRECTORY = Main.SETS_DIRECTORY;
    private static final String CONDITIONS_DIRECTORY = Main.CONDITIONS_DIRECTORY;
    private static final Logger logger = Main.logger;

    @Override
    public void run() {
        createConditionsDirectory();
        createConditions();
    }

    private void createConditionsDirectory() {
        Path path = Paths.get(CONDITIONS_DIRECTORY);
        deleteFolderIfExists(path);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new CustomIOException("Could not create Conditions Directory", e);
        }
    }

    private void createConditions() {
        File[] sets = getFileList(new File(SETS_DIRECTORY));
        iterateSets(sets);
    }

    private void iterateSets(File[] sets) {
        for (File set : sets) {
            if(set.isDirectory()) {
                logger.info("Found set: " + set.getName());
                Config config = new Config(set);
                config.assertValid();
                File[] files = getFileList(set);
                if(config.isLooseFiles()) {
                    iterateLooseAnimations(files, config);
                } else {
                    iteratePacks(files, config);
                }
            }
        }
    }

    private void iterateLooseAnimations(File[] animations, Config config) {
        for (File animation : animations) {
            if(animation.isFile() && isHkxFile(animation)) {
                logger.info("Found loose animation file " + animation.getName() + " within set");
                config.getConditions().generateNextCondition();
                config.getConditions().copyToCondition(animation, config.getTargetFileName());
            }
        }
    }

    private void iteratePacks(File[] packs, Config config) {
        for (File pack : packs) {
            if(pack.isDirectory()) {
                logger.info("Found pack " + pack.getName() + " within set");
                config.getConditions().generateNextCondition();
                File[] files = getFileList(pack);
                iteratePackFiles(files, config, pack);
            }
        }
    }

    private void iteratePackFiles(File[] files, Config config, File topPack) {
        for (File file : files) {
            if(file.isDirectory()) {
                logger.info("Found folder " + file.getName() + " within pack");
                files = getFileList(file);
                iteratePackFiles(files, config, topPack);
            }
            if (file.isFile() && isHkxFile(file)) {
                logger.info("Found animation file " + file.getName() + " within pack");
                Path relativePath = topPack.toPath().relativize(file.toPath());
                config.getConditions().copyToCondition(file, relativePath.toString());
            }
        }
    }

    private File[] getFileList(File folder) {
        File[] list = folder.listFiles();
        if(list == null) {
            throw new CustomIOException("Could not get list of files in " + folder.getAbsolutePath());
        }
        return list;
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
                    throw new CustomIOException("Could not delete folder " + path);
                }
            }
        } catch (IOException e) {
            throw new CustomIOException("Could not delete path " + path.toAbsolutePath(), e);
        }

    }
}
