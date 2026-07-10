package io.dbflow.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeHelper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeHelper() {
    }

    public static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
