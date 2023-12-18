package ru.naumen.personalfinancebot.message;

/**
 * Класс для хранения статических сообщений/шаблонов сообщений для пользователя
 */
public class Message {
    /**
     * Сообщение о неверно переданной категории
     */
    public static final String INCORRECT_CATEGORY_ARGUMENT_FORMAT = "Название категории введено неверно. Оно может " +
            "содержать от 1 до 64 символов латиницы, кириллицы, цифр, тире и пробелов";


    /**
     * Сообщение о неверно введённой дате (месяц и год)
     */
    public static final String INCORRECT_BUDGET_YEAR_MONTH = "Дата введена неверно! Введите ее в формате " +
            "[mm.yyyy - месяц.год]";

    /**
     * Сообщение о некорректно переданной сумме бюджета при его создании
     */
    public static final String INCORRECT_BUDGET_NUMBER_ARG = "Все суммы должны быть больше нуля!";

}
