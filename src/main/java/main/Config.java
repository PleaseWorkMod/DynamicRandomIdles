package main;

import exception.ConfigException;
import exception.CustomIOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {
    private static final String SETS_DIRECTORY = Main.SETS_DIRECTORY;
    private static final String CONFIG_NAME = Main.CONFIG_NAME;
    private static final Logger logger = Main.logger;


    private final File set;
    private boolean looseFiles;
    private String targetFileName;
    private final Conditions conditions;

    public Config(File set) {
        this.looseFiles = false;
        this.conditions = new Conditions(set);
        this.set = set;

        Path path = Paths.get(SETS_DIRECTORY);
        loadFor(path);
        path = this.set.toPath();
        loadFor(path);
    }

    private void loadFor(Path set) {
        try {
            Path path = Paths.get(set.toString(), CONFIG_NAME);
            logger.info("Loading Config file " + path);

            Properties properties = new Properties();
            FileInputStream in = new FileInputStream(path.toString());
            properties.load(in);
            in.close();

            parseProperties(properties);
        } catch (IOException e) {
            throw new CustomIOException("Could not load config file", e);
        }
    }

    private void parseProperties(Properties properties) {
        if(properties.containsKey("looseFiles")) {
            looseFiles = Boolean.parseBoolean(properties.getProperty("looseFiles"));
        }
        if(properties.containsKey("targetFileName")) {
            targetFileName = properties.getProperty("targetFileName");
        }
        if(properties.containsKey("conditions")) {
            String condition = properties.getProperty("conditions");
            conditions.setCondition(condition);
        }
        if(properties.containsKey("conditionBaseIndex")) {
            int conditionBaseIndex = Integer.parseInt(properties.getProperty("conditionBaseIndex"));
            conditions.setConditionBaseIndex(conditionBaseIndex);
        }
    }

    public Conditions getConditions() {
        return conditions;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public boolean isLooseFiles() {
        return looseFiles;
    }

    public void assertValid() {
        conditions.assertValid();

        if(looseFiles && targetFileName == null) {
            throw new ConfigException("Set '" + set.getName() + "': Property looseFiles was set to true, so property targetFileName has to be set as well.");
        }
        if(!looseFiles && targetFileName != null) {
            throw new ConfigException("Set '" + set.getName() + "': Property looseFiles was set to false, so the set property targetFileName has no effect. Terminating for safety.");
        }
    }
}
