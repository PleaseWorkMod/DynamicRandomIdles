package main;

import java.util.logging.Logger;

public class Main {
    public static final Logger logger = new LoggerBuilder().build();

    public static class Pc {
        public static void main(String[] args) {
            new Generator(ConditionType.PC).run();
        }
    }

    public static class PcNpc {
        public static void main(String[] args) {
            new Generator(ConditionType.PC_NPC).run();
        }
    }

    public static class PcNpcElders {
        public static void main(String[] args) {
            new Generator(ConditionType.PC_NPC_ELDERS).run();
        }
    }
}
