package ru.naumen.personalfinancebot.service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Сервис для форматирования чисел
 */
public class OutputNumberFormatService {
    /**
     * Форматировщик для double
     * Он убирает дробную часть при ее отсутствии
     * И разделяет пробелами разряды числа
     */
    private final DecimalFormat doubleFormatter;

    public OutputNumberFormatService() {
        this.doubleFormatter = this.createDoubleFormatter();
    }

    /**
     * Форматирует double в красивую строку.
     * Если число целое, то вернет его без дробной части.
     * <br>
     * Т.е. 1000.0 будет выведено как 1000,
     * а 1000.99 будет выведено как 1000.99
     */
    public String formatDouble(double d) {
        return doubleFormatter.format(d);
    }

    /**
     * Форматирует double в красивую строку.
     * Если число целое, то вернет его без дробной части.
     * <br>
     * Т.е. 1000.0 будет выведено как 1000,
     * а 1000.99 будет выведено как 1000.99.
     * <br>
     * Если d будет null, тогда будет выведено defaultValue
     */
    public String formatDouble(Double d, String defaultValue) {
        if (d == null) {
            return defaultValue;
        }
        return formatDouble(d);
    }

    /**
     * Создает форматировщик для double
     * Он убирает дробную часть при ее отсутствии
     * И разделяет пробелами разряды числа
     */
    private DecimalFormat createDoubleFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(symbols);
        format.setGroupingSize(3);
        return format;
    }
}
