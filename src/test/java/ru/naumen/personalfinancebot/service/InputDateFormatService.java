package ru.naumen.personalfinancebot.service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Сервис, который форматирует дату для подачи ее в аргументы команды
 */
public class InputDateFormatService {
    /**
     * Форматировщик месяца-год к виду "01.2024"
     */
    private final DateTimeFormatter dotFormatter;

    public InputDateFormatService() {
        this.dotFormatter = DateTimeFormatter.ofPattern("MM.yyyy");
    }

    /**
     * Форматирует месяц-год к виду "01.2024"
     *
     * @param yearMonth Месяц-год
     * @return строка
     */
    public String formatYearMonth(YearMonth yearMonth) {
        return yearMonth.format(this.dotFormatter);
    }
}
