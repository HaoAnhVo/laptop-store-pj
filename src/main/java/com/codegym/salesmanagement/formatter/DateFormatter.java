package com.codegym.salesmanagement.formatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatter {
    private static final String DEFAULT_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    public static String formatDateTime(LocalDateTime dateTime) {
        return formatDateTime(dateTime, DEFAULT_DATE_TIME_FORMAT);
    }

    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
}
