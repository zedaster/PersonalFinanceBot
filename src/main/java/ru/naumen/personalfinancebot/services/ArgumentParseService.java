package ru.naumen.personalfinancebot.services;

import com.sun.istack.Nullable;
import ru.naumen.personalfinancebot.messages.StaticMessages;

import java.time.YearMonth;
import java.util.List;

/**
 * Сервис, форматирующий аргументы
 */
public class ArgumentParseService {
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
            throw new IllegalArgumentException();
        }
        return amount;
    }

    /**
     * Парсит категорию, введенную в аргументах
     *
     * @throws IllegalArgumentException если аргументы введены неверно
     */
    public String parseCategory(List<String> args) throws IllegalArgumentException {
        if (args.size() != 1) {
            throw new IllegalArgumentException(StaticMessages.INCORRECT_CATEGORY_ARGUMENT_COUNT);
        }

        String categoryName = beautifyCategoryName(args.get(0));
        if (!isValidCategory(categoryName)) {
            throw new IllegalArgumentException(StaticMessages.INCORRECT_CATEGORY_ARGUMENT_FORMAT);
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
     * либо пробелом.
     *
     * @param categoryName Название категории
     * @return true/false в зависимости от валидности названия категории
     */
    private boolean isValidCategory(String categoryName) {
        return categoryName.matches("^[A-Za-zА-Яа-я0-9 ]{1,64}$");
    }

    /**
     * Парсит полученный аргумент и возвращает экземпляр класса YearMonth
     *
     * @param argument
     * @return YearMonth
     */
    public YearMonth parseYearMonth(String argument) {
        return null;
    }

    /**
     * Парсит полученный аргумент и возвращает число с типом double
     *
     * @param argument
     * @return Положительное число
     */
    public double parsePositiveDouble(String argument) {
        return 0.0;
    }
}
