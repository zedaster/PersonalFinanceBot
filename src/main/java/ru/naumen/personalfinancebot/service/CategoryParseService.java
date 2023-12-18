package ru.naumen.personalfinancebot.service;

import ru.naumen.personalfinancebot.message.Message;

import java.util.List;

/**
 * Сервис, который парсит категорию
 */
public class CategoryParseService {
    /**
     * Сообщение о неверно переданном количестве аргументов для команды /add_[income|expense]_category
     */
    private static final String INCORRECT_CATEGORY_ARGUMENT_COUNT =
            "Данная команда принимает [название категории] в одно или несколько слов.";

    /**
     * Парсит категорию, введенную в аргументах
     *
     * @throws IllegalArgumentException если аргументы введены неверно
     */
    public String parseCategory(List<String> args) throws IllegalArgumentException {
        String joinedString;
        if (args.isEmpty() || (joinedString = String.join(" ", args).trim()).isEmpty()) {
            throw new IllegalArgumentException(INCORRECT_CATEGORY_ARGUMENT_COUNT);
        }

        String categoryName = beautifyCategoryName(joinedString);
        if (!isValidCategory(categoryName)) {
            throw new IllegalArgumentException(Message.INCORRECT_CATEGORY_ARGUMENT_FORMAT);
        }
        return categoryName;
    }

    /**
     * Делает красивым и корректным имя категории.
     * Убирает пробелы в начале и заменяет множественные пробелы посередине на одиночные.
     * Первую букву делает заглавной, остальные - маленькими.
     *
     * @param text Строка для обработки
     * @return Новая строка
     */
    private String beautifyCategoryName(String text) {
        char[] newChars = text
                .trim()
                .replaceAll("\\s{2,}", " ")
                .toLowerCase()
                .toCharArray();
        if (newChars.length == 0) {
            return "";
        }
        newChars[0] = Character.toUpperCase(newChars[0]);
        return String.valueOf(newChars);
    }

    /**
     * Проверяет, соответствует ли название категории правильному формату.
     * Символов должно быть от 1 до 64, каждый должен являться либо буквой в кириллице, латинице, либо цифрой,
     * либо пробелом, либо тире
     *
     * @param categoryName Название категории
     * @return true/false в зависимости от валидности названия категории
     */
    private boolean isValidCategory(String categoryName) {
        return categoryName.matches("^[A-Za-zА-Яа-я0-9\\- ]{1,64}$");
    }
}
