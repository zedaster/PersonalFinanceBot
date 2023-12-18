package ru.naumen.personalfinancebot.service;

import com.sun.istack.Nullable;
import ru.naumen.personalfinancebot.message.Message;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
            throw new IllegalArgumentException("Баланс не может быть отрицательным.");
        }
        return amount;
    }

    /**
     * Парсит категорию, введенную в аргументах
     *
     * @throws IllegalArgumentException если аргументы введены неверно
     */
    public String parseCategory(List<String> args) throws IllegalArgumentException {
        String joinedString;
        if (args.isEmpty() || (joinedString = String.join(" ", args).trim()).isEmpty()) {
            throw new IllegalArgumentException(Message.INCORRECT_CATEGORY_ARGUMENT_COUNT);
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

    /**
     * Парсит полученный аргумент и возвращает экземпляр класса YearMonth
     *
     * @param argument
     * @return YearMonth
     */
    public YearMonth parseYearMonth(String argument) throws DateTimeParseException {
        return YearMonth.parse(argument, DateTimeFormatter.ofPattern("MM.yyyy"));
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

    /**
     * Парсит год
     *
     * @param argument строка
     * @return год в виде int
     * @throws NumberFormatException если год введен некорректно, либо он меньше нуля, либо больше 3000
     */
    public int parseYear(String argument) throws NumberFormatException {
        int year = Integer.parseInt(argument);
        if (year < 0 || year > 3000) {
            throw new NumberFormatException("Год не может выходить за пределы диапазона [0, 3000].");
        }
        return year;
    }
}
