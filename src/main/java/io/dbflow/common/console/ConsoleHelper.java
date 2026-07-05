package io.dbflow.common.console;

public class ConsoleHelper {

    public static void success(String message) {
        System.out.println();
        System.out.println("✅ " + message);
    }

    public static void error(String message) {
        System.out.println();
        System.err.println("❌ " + message);
    }

    public static void info(String message) {
        System.out.println("ℹ️ " + message);
        System.out.println();
    }
}
