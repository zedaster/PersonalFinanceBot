package ru.naumen.personalfinancebot.repository.category.exception;

/**
 * Исключение, выбрасываемое в случае, если категория не существует
 */
public class NotExistingCategoryException extends Exception {
    /**
     * @param categoryName Название категории
     */
    public NotExistingCategoryException(String categoryName) {
        super("Категория '%s' не существует!".formatted(categoryName));
    }
}
