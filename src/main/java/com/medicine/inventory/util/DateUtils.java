package com.medicine.inventory.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static LocalDate parse(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String format(LocalDate date) {
        return date != null ? date.format(FORMATTER) : "";
    }

    public static boolean isValid(String dateStr) {
        return parse(dateStr) != null;
    }

    public static long daysUntil(LocalDate target) {
        if (target == null) return Long.MAX_VALUE;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), target);
    }
}