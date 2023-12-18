package ru.naumen.personalfinancebot.service;

import com.sun.istack.Nullable;

import java.util.List;

/**
 * Сервис, который парсит числа
 */
public class NumberParseService {
    /**
     * Парсит баланс, введенный пользователем.
     * Вернет null если баланс не является числом с плавающей точкой или меньше нуля
     *
     * @throws IllegalArgumentException если аргументы введены неверно
     */
    @Nullable
    public Double parseBalance(List<String> args) throws IllegalArgumentException {
        if (args.size() != 1) {
            throw new IllegalArgumentException();
        }
        String parsedString = args.get(0);
        double amount = Double.parseDouble(parsedString.replace(",", "."));
        if (amount < 0) {
            throw new IllegalArgumentException("Баланс не может быть отрицательным.");
        }
        return amount;
    }

    /**
     * Парсит полученный аргумент и возвращает число с типом double
     *
     * @param argument Строка, из которой парситься число
     * @return Положительное число
     */
    public double parsePositiveDouble(String argument) throws NumberFormatException {
        // To prevent NaN and 10e-7
        if (!argument.matches("^[0-9.]+$")) {
            throw new NumberFormatException("The argument to parse double may contain only dot and digits");
        }
        double parsedDouble = Double.parseDouble(argument);
        if (parsedDouble <= 0) {
            throw new NumberFormatException("The parsed double must be bigger than zero!");
        }
        return parsedDouble;
    }
}
