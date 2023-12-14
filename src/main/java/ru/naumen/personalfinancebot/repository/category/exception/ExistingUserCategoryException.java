package ru.naumen.personalfinancebot.repository.category.exception;

/**
 * Исключение, выбрасываемое в случае, если уже есть пользовательская категория с таким же названием.
 */
public class ExistingUserCategoryException extends Exception {
    /**
     * @param categoryName Название категории
     */
    public ExistingUserCategoryException(String categoryName) {
        super("Уже существует пользовательская категория с названием '%s'".formatted(categoryName));
    }
}
