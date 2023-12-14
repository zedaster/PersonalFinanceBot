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
     * Сообщение об успешном удалении пользовательской категориии
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
     * Сообщение о неверно переданном количестве аргументов для команды /add_[income|expense]
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
     * Сообщение об отсутствии данных по затратам
     */
    public static final String EXPENSES_NOT_EXIST = "К сожалению, данные по затратам отсутствуют";

    /**
     * Шаблон строки отчета для команды /report_expense
     */
    public static final String EXPENSE_REPORT_PATTERN = "{category}: {payment} руб.\n";

    /**
     * Сообщение о неверно введённой дате (месяц и год)
     */
    public static final String INCORRECT_BUDGET_YEAR_MONTH = "Дата введена неверно! Введите ее в формате " +
            "[mm.yyyy - месяц.год]";

    /**
     * Сообщение о некорректно переданной сумме бюджета при его создании
     */
    public static final String INCORRECT_BUDGET_NUMBER_ARG = "Все суммы должны быть больше нуля!";

    /**
     * Сообщение о некорректно переданном годе для создания бюджета.
     */
    public static final String INCORRECT_BUDGET_YEAR_ARG = "Год введен неверно! Дальше 3000 года не стоит планировать.";

    /**
     * Сообщение, предоставляющее пользователю информацию о доступных командах для пользователя
     */
    public static final String BUDGET_HELP = """
            Доступные команды для работы с бюджетами:
            /budget - показать бюджет за текущий месяц
            /budget_list - запланированный бюджет за последние 12 месяцев
            /budget_list [yyyy - год] -  запланированные бюджеты за определенный год
            /budget_list [mm.yyyy from - месяц от] [mm.year to - месяц до] - запланированные бюджеты за определенные месяца
            /budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы] - планировать бюджет""";

    /**
     * Сообщение о неверно введенной команде /budget_create
     */
    public static final String INCORRECT_CREATE_BUDGET_ENTIRE_ARGS = "Неверно введена команда! Введите " +
            "/budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы]";

    /**
     * Сообщение об ошибке в случае если пользователь планирует бюджет на прошлое.
     */
    public static final String CANT_CREATE_OLD_BUDGET = "Вы не можете создавать бюджеты за прошедшие месяцы!";

    /**
     * Шаблон сообщения для вывода сообщения о созданном бюджете
     */
    public static final String BUDGET_CREATED = """
            Бюджет на {month} {year} создан.
            Ожидаемые доходы: {expect_income}
            Ожидаемые расходы: {expect_expenses}
            Текущие доходы: {current_income}
            Текущие расходы: {current_expenses}
            Текущий баланс: {balance}
            Нужно еще заработать: {income_left}
            Еще осталось на траты: {expenses_left}""";

    /**
     * Сообщение о неверно введенной команде при редактировании бюджета
     */
    public static final String INCORRECT_EDIT_BUDGET_ENTIRE_ARGS = "Неверно введена команда! Введите " +
            "/budget_set_[income/expenses] [mm.yyyy - месяц.год] [ожидаемый доход/расход]";

    /**
     * Сообщение об отсутствии бюджета на указанную пользователем дату
     */
    public static final String BUDGET_NOT_FOUND = "Бюджет на этот период не найден! Создайте его командой " +
            "/budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы]";

    /**
     * Сообщение об ошибке при редактировании бюджета за прошедшие месяцы
     */
    public static final String CANT_EDIT_OLD_BUDGET = "Вы не можете изменять бюджеты за прошедшие месяцы!";

    /**
     * Шаблон сообщения при успешном редактировании бюджета
     */
    public static final String BUDGET_EDITED = """
            Бюджет на {month} {year} изменен:
            Ожидаемые доходы: {expect_income}
            Ожидаемые расходы: {expect_expenses}""";

    /**
     * Сообщение о неверно введенной команде /budget_list
     */
    public static final String INCORRECT_LIST_BUDGET_ENTIRE_ARGS = """
            Неверно введена команда! Введите
            или /budget_list - вывод бюджетов за 12 месяцев (текущий + предыдущие),
            или /budget_list [год] - вывод бюджетов за определенный год,
            или /budget_list [mm.yyyy - месяц.год] [mm.yyyy - месяц.год] - вывод бюджетов за указанный промежуток.""";

    /**
     * Сообщение об ошибке если передана дата начала, которая позднее даты конца
     */
    public static final String BUDGET_LIST_FROM_IS_AFTER_TO = "Дата начала не может быть позднее даты конца периода!";

    /**
     * Префикс для списка запланированных бюджетов.
     */
    public static final String BUDGET_LIST_PREFIX = "Ваши запланированные доходы и расходы по месяцам:";

    /**
     * Шаблон для вывода бюджета за конкретный год и месяц
     */
    public static final String BUDGET_LIST_ELEMENT = """
            {month} {year}:
            Ожидание: + {expect_income} | - {expect_expenses}
            Реальность: + {real_income} | - {real_expenses}""";

    /**
     * Постфикс для сообщения пользователю при выводе списка бюджетов за n-ое кол-во месяцев
     */
    public static final String BUDGET_LIST_RANGE_POSTFIX = "Данные показаны за {count} месяц(-ев).";

    /**
     * Постфикс для сообщения пользователю при выводе списка бюджетов за конкретный год
     */
    public static final String BUDGET_LIST_YEAR_POSTFIX = "Данные показаны за {year} год.";

    /**
     * Сообщение для вывода списка бюджетов за последние 12 месяцев.
     */
    public static final String BUDGET_LIST_TWELVE_MONTHS_POSTFIX = "Данные показаны за последние 12 месяцев. " +
            "Чтобы посмотреть данные, например, за 2022, введите /budget_list 2022.\n" +
            "Для показа данных по определенным месяцам, например, с ноября 2022 по январь 2023 введите " +
            "/budget_list 10.2022 01.2023";

    /**
     * Сообщение об отсутствии бюджетов за указанный пользователем период
     */
    public static final String BUDGET_LIST_EMPTY = "У вас не было бюджетов за этот период. Для создания бюджета " +
            "введите /budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы]";

    /**
     * Сообщение об отсутствии бюджета за конкретную дату
     */
    public static final String CURRENT_BUDGET_NOT_EXISTS = "Бюджет на {month} {year} отсутствует";

    /**
     * Шаблон сообщения для вывода текущего бюджета
     */
    public static final String CURRENT_BUDGET = """
            Бюджет на {month} {year}:
            Ожидаемые доходы: {expect_income}
            Ожидаемые расходы: {expect_expenses}
            Текущие доходы: {real_income}
            Текущие расходы: {real_expenses}
            Текущий баланс: {balance}
            Нужно еще заработать: {income_left}
            Еще осталось на траты: {expenses_left}""";

    public static final String DATA_NOT_EXISTS = "На заданный промежуток данные отсутствуют.";

    public static final String CURRENT_DATA_NOT_EXISTS = "На этот месяц данные отсутствуют.";

    public static final String ESTIMATE_REPORT_INCORRECT_ARGUMENT_COUNT = """
            Команда "/estimate_report" не принимает аргументов, либо принимает Месяц и Год в формате "MM.YYYY".
            Например, "/estimate_report" или "/estimate_report 12.2023".""";

    public static final String ESTIMATE_REPORT_CURRENT = """
            Подготовил отчет по средним доходам и расходам пользователей за текущий месяц:
            Расходы: {expenses}
            Доходы: {income}""";

    public static final String ESTIMATE_REPORT_DATED = """
            Подготовил отчет по средним доходам и расходам пользователей за {month} {year}:
            Расходы: {expenses}
            Доходы: {income}""";
}
