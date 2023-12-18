package ru.naumen.personalfinancebot.service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Сервис, который парсит дату (Год или Месяц-Год)
 */
public class DateParseService {
    /**
     * Парсит полученный аргумент и возвращает экземпляр класса YearMonth
     *
     * @param argument
     * @return YearMonth
     */
    public YearMonth parseYearMonth(String argument) throws DateTimeParseException {
        return YearMonth.parse(argument, DateTimeFormatter.ofPattern("MM.yyyy"));
    }

    /**
     * Парсит год
     *
     * @param argument строка
     * @return год в виде int
     * @throws NumberFormatException если год введен некорректно, либо он меньше нуля, либо больше 3000
     */
    public int parseYear(String argument) throws NumberFormatException {
        int year = Integer.parseInt(argument);
        if (year < 0 || year > 3000) {
            throw new NumberFormatException("Год не может выходить за пределы диапазона [0, 3000].");
        }
        return year;
    }
}
