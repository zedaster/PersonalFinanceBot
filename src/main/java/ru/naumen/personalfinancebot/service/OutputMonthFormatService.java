package ru.naumen.personalfinancebot.service;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Сервис для форматирования названия месяца
 */
public class OutputMonthFormatService {
    /**
     * Выводит русское название месяца с заглавной буквы.
     * Например, "Январь" или "Февраль"
     */
    public String formatRuMonthName(Month month) {
        Locale loc = Locale.forLanguageTag("ru");
        String name = month.getDisplayName(TextStyle.FULL_STANDALONE, loc);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
