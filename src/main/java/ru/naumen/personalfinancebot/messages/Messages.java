package ru.naumen.personalfinancebot.messages;

/**
 * Класс для хранения статических сообщений/шаблонов сообщений для пользователя
 */
public class Messages {
    public static final String WELCOME_MESSAGE = "Добро пожаловать в бота для управления финансами!";

    public static final String COMMAND_NOT_FOUND = "Команда не распознана...";

    public static final String SET_BALANCE_SUCCESSFULLY = "Ваш баланс изменен. Теперь он составляет {balance}";

    public static final String ADD_INCOME_MESSAGE = "Вы успешно добавили доход по источнику: ";
    public static final String ADD_EXPENSE_MESSAGE = "Добавлен расход по категории: ";

    public static final String INCORRECT_CATEGORY_ARGUMENT_COUNT =
            "Данная команда принимает 1 аргумент: [название категории]";
    public static final String INCORRECT_CATEGORY_ARGUMENT_FORMAT = "Название категории введено неверно. Оно может " +
            "содержать от 1 до 64 символов латиницы, кириллицы, цифр и пробелов";

    public static final String USER_CATEGORY_ALREADY_EXISTS = "Персональная категория {type} '{name}' уже существует.";
    public static final String STANDARD_CATEGORY_ALREADY_EXISTS = "Стандартная категория {type} '{name}' уже " +
            "существует.";
    public static final String USER_CATEGORY_ADDED = "Категория {type} '{name}' успешно добавлена";

    public static final String USER_CATEGORY_ALREADY_NOT_EXISTS = "Пользовательской категории {type} '{name}' не " +
            "существует!";
    public static final String USER_CATEGORY_REMOVED = "Категория {type} '{name}' успешно удалена";

    public static final String LIST_TYPED_CATEGORIES = """
            Все доступные вам категории {type}:
            Стандартные:
            {standard_list}
            Персональные:
            {personal_list}""";
    public static final String EMPTY_LIST_CONTENT = "<отсутствуют>";


    public static final String INCORRECT_OPERATION_ARGS_AMOUNT =
            "Данная команда принимает 2 аргумента: [payment - сумма] [категория расхода/дохода]";

    public static final String CATEGORY_DOES_NOT_EXISTS =
            "Указанная категория не числится. Используйте команду /add_[income/expense]_category чтобы добавить её";

    public static final String INCORRECT_SELF_REPORT_ARGS =
            "Команда /report_expense принимает 1 аргумент [mm.yyyy], например \"/report_expense 11.2023\"";

    public static final String INCORRECT_SELF_REPORT_VALUES = """
    Переданы неверные данные месяца и года.
    Дата должна быть передана в виде "MM.YYYY", например, "11.2023".""";

    public static final String SELF_REPORT_MESSAGE = "Подготовил отчёт по вашим расходам за указанный месяц:\n";

    public static final String INCORRECT_PAYMENT_ARG = "Сумма операции указана в неверном формате.";

    public static final String ILLEGAL_PAYMENT_ARGUMENT = "Ошибка! Аргумент [payment] должен быть больше 0";

    public static final String EXPENSES_NOT_EXIST = "К сожалению, данные по затратам отсутствуют";

    public static final String EXPENSE_REPORT_PATTERN = "{category}: {payment} руб.\n";
}
