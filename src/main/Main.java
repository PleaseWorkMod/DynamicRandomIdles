package main;

import java.util.logging.Logger;

public class Main {
    public static final Logger logger = new LoggerBuilder().build();

    public static void main(String[] args) {
        new FileBuilder().build();
    }

}
