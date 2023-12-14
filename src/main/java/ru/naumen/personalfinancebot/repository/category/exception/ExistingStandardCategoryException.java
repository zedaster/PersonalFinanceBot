package ru.naumen.personalfinancebot.repository.category.exception;

/**
 * Исключение, выбрасываемое в случае, если уже есть стандартная категория с таким же названием.
 */
public class ExistingStandardCategoryException extends Exception {
    /**
     * @param categoryName Название категории
     */
    public ExistingStandardCategoryException(String categoryName) {
        super("Уже существует стандартная категория с названием '%s'".formatted(categoryName));
    }
}