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
     * @throws IllegalArgumentException если аргументов более одного
     * @throws NumberFormatException если аргумент с числом введен неверно
     */
    @Nullable
    public Double parseBalance(List<String> args) throws IllegalArgumentException {
        if (args.size() != 1) {
            throw new IllegalArgumentException("Должен быть только 1 аргумент!");
        }
        double amount = parseCorrectDouble(args.get(0));
        if (amount < 0) {
            throw new NumberFormatException("Баланс не может быть отрицательным.");
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
        double parsedDouble = parseCorrectDouble(argument);
        if (parsedDouble <= 0) {
            throw new NumberFormatException("Число должно быть положительным!");
        }
        return parsedDouble;
    }

    /**
     * Парсит double число с 2-мя знаками после запятой или точки
     *
     * @param argument строка для парсинга
     * @return double число
     * @throws NumberFormatException если введены некорректные символы или больше 2-х знаков после запятой/точки
     */
    private double parseCorrectDouble(String argument) throws NumberFormatException {
        // To prevent NaN and 10e-7
        if (!argument.matches("^[0-9,.]+$")) {
            throw new NumberFormatException("Введены неверные символы!");
        }
        double parsedDouble = Double.parseDouble(argument.replace(",", "."));

        if (Math.round(parsedDouble * 100) != parsedDouble * 100) {
            throw new NumberFormatException("Разрешено вводить только целые числа или дробные до 2-х знаков после" +
                                            " запятой.");
        }
        return parsedDouble;
    }
}
