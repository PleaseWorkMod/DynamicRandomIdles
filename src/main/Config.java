package main;

import exception.CustomRuntimeException;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {
    private static final String CONFIG_NAME = "_config.properties";
    private static final Logger logger = Main.logger;

    private Conditions conditions;
    private String targetFileName;

    public Config() {
        conditions = new Conditions();

        Path path = Paths.get(Generator.SETS_DIRECTORY);
        loadFor(path);
    }

    public void loadFor(Path set) {
        try {
            Path path = Paths.get(set.toString(), CONFIG_NAME);
            logger.info("Loading Config file " + path);

            Properties properties = new Properties();
            FileInputStream in = new FileInputStream(path.toString());
            properties.load(in);
            in.close();

            parseProperties(properties);
        } catch (IOException e) {
            throw new CustomRuntimeException("Could not load config file", e);
        }
    }

    private void parseProperties(Properties properties) {
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
}
