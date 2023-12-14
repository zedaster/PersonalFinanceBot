package ru.naumen.personalfinancebot.message;

/**
 * Класс для хранения статических сообщений/шаблонов сообщений для пользователя
 */
public class Message {
    /**
     * Сообщение - приветствие для пользователя
     */
    public static final String WELCOME_MESSAGE = "Добро пожаловать в бота для управления финансами!";

    /**
     * Сообщение, если пользователь передал неверную команду
     */
    public static final String COMMAND_NOT_FOUND = "Команда не распознана...";

    /**
     * Сообщение для команды /set_balance
     */
    public static final String SET_BALANCE_SUCCESSFULLY = "Ваш баланс изменен. Теперь он составляет {balance}";

    /**
     * Сообщение об успешном добавлении дохода для пользователя
     */
    public static final String ADD_INCOME_MESSAGE = "Вы успешно добавили доход по источнику: ";
    /**
     * Сообщение об успешном добавлении расхода для пользователя
     */
    public static final String ADD_EXPENSE_MESSAGE = "Добавлен расход по категории: ";

    /**
     * Сообщение о неверно переданном количестве аргументов для команды /add_[income|expense]_category
     */
    public static final String INCORRECT_CATEGORY_ARGUMENT_COUNT =
            "Данная команда принимает [название категории] в одно или несколько слов.";
    /**
     * Сообщение о неверно переданной категории
     */
    public static final String INCORRECT_CATEGORY_ARGUMENT_FORMAT = "Название категории введено неверно. Оно может " +
            "содержать от 1 до 64 символов латиницы, кириллицы, цифр, тире и пробелов";

    /**
     * Сообщение о существовании персональной (пользовательской) категории
     */
    public static final String USER_CATEGORY_ALREADY_EXISTS = "Персональная категория {type} '{name}' уже существует.";

    /**
     * Сообщение о существовании стандартной категории
     */
    public static final String STANDARD_CATEGORY_ALREADY_EXISTS = "Стандартная категория {type} '{name}' уже " +
            "существует.";
    /**
     * Сообщение об успешно созданной пользовательской категории
     */
    public static final String USER_CATEGORY_ADDED = "Категория {type} '{name}' успешно добавлена";

    /**
     * Сообщение об отсутствии пользовательской категории
     */
    public static final String USER_CATEGORY_ALREADY_NOT_EXISTS = "Пользовательской категории {type} '{name}' не " +
            "существует!";

    /**
     * Сообщение об успещном удалении пользовательской категориии
     */
    public static final String USER_CATEGORY_REMOVED = "Категория {type} '{name}' успешно удалена";

    /**
     * Шаблон для вывода сообщения о доступных пользователю категориях
     */
    public static final String LIST_TYPED_CATEGORIES = """
            Все доступные вам категории {type}:
            Стандартные:
            {standard_list}
            Персональные:
            {personal_list}""";

    /**
     * Часть шаблона списка категорий на случай, если категории отсутствуют
     */
    public static final String EMPTY_LIST_CONTENT = "<отсутствуют>";

    /**
     * Сообщение о неверном переданном количестве аргументов для команды /add_[income|expense]
     */
    public static final String INCORRECT_OPERATION_ARGS_AMOUNT =
            "Данная команда принимает 2 аргумента: [payment - сумма] [категория расхода/дохода]";

    /**
     * Сообщение об отсутствии категории
     */
    public static final String CATEGORY_DOES_NOT_EXISTS =
            "Указанная категория не числится. Используйте команду /add_[income/expense]_category чтобы добавить её";

    /**
     * Сообщение о неверно переданном количестве аргументов для команды /report_expense.
     */
    public static final String INCORRECT_SELF_REPORT_ARGS =
            "Команда /report_expense принимает 1 аргумент [mm.yyyy], например \"/report_expense 11.2023\"";

    /**
     * Сообщение о неверно переданной дате (месяц и год) для команды /report_expense
     */
    public static final String INCORRECT_SELF_REPORT_VALUES = """
    Переданы неверные данные месяца и года.
    Дата должна быть передана в виде "MM.YYYY", например, "11.2023".""";

    /**
     * Начало отчета по расходам пользователя
     */
    public static final String SELF_REPORT_MESSAGE = "Подготовил отчёт по вашим расходам за указанный месяц:\n";

    /**
     * Сообщение о неверно переданном аргументе, который отвечает за сумму операции
     */
    public static final String INCORRECT_PAYMENT_ARG = "Сумма операции указана в неверном формате. Передайте корректное положительно число";

    /**
     * Сообщение об отсутсвии данных по затратам
     */
    public static final String EXPENSES_NOT_EXIST = "К сожалению, данные по затратам отсутствуют";

    /**
     * Шаблон строки отчета для команды /report_expense
     */
    public static final String EXPENSE_REPORT_PATTERN = "{category}: {payment} руб.\n";
}
