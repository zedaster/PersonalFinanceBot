package ru.naumen.personalfinancebot.messages;

public class StaticMessages {
    public static final String ADD_INCOME_MESSAGE = "Вы успешно добавили доход по источнику: ";
    public static final String ADD_EXPENSE_MESSAGE = "Добавлен расход по категории: ";

    public static final String INCORRECT_OPERATION_ARGS_AMOUNT =
            "Данная команда принимает 2 аргумента: [payment - сумма] [категория расхода/дохода]";

    public static final String CATEGORY_DOES_NOT_EXISTS =
            "Указанная категория не числится. Используйте команду /add_category чтобы добавить её";

    public static final String INCORRECT_SELF_REPORT_ARGS =
            "Команда /report_expense принимает 1 аргумент [mm.yyyy], например /set_expense 11.2023";

    public static final String INCORRECT_SELF_REPORT_VALUES = "Переданы неверные данные";
}
