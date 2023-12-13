package ru.naumen.personalfinancebot.repository.category.exceptions;

/**
 * Исключение, генерируемое при попытке создания стандартной или пользовательской категории, если уже есть
 * стандартная категория с таким же названием.
 */
public class CreatingExistingStandardCategoryException extends CreatingExistingCategoryException {
    /**
     * @param message      Сообщение ооб ошибке
     * @param categoryName Название категории
     */
    public CreatingExistingStandardCategoryException(String message, String categoryName) {
        super(message, categoryName);
    }
}