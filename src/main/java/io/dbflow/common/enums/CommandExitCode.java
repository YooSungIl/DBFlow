package io.dbflow.common.enums;

public enum CommandExitCode {
    SUCCESS(0),
    EXECUTION_ERROR(1),
    USAGE_ERROR(2);

    private final int value;

    CommandExitCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
