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
    public static final String INCORRECT_YEAR_MONTH_FORMAT = "Дата введена неверно! Введите ее в формате " +
            "[mm.yyyy - месяц.год]";

    /**
     * Сообщение о некорректно переданной сумме бюджета при его создании
     */
    public static final String INCORRECT_BUDGET_NUMBER_ARG = "Все суммы должны быть больше нуля!";

    /**
     * Часть шаблона списка категорий на случай, если категории отсутствуют
     */
    public static final String EMPTY_LIST_CONTENT = "<отсутствуют>";

    /**
     * Сообщение об отсутствии данных на заданный промежуток
     */
    public static final String DATA_NOT_EXISTS = "На заданный промежуток данные отсутствуют.";

    /**
     * Сообщение об отсутствии данных на текущий промежуток
     */
    public static final String CURRENT_DATA_NOT_EXISTS = "На этот месяц данные отсутствуют.";
}
