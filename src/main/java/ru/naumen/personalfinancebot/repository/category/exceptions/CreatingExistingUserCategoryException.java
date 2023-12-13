package ru.naumen.personalfinancebot.repository.category.exceptions;

/**
 * Исключение, генерируемое при попытке создания пользовательской категории, если она уже
 * существует как пользовательская с таким же названием.
 */
public class CreatingExistingUserCategoryException extends CreatingExistingCategoryException {
    /**
     * @param message      Сообщение ооб ошибке
     * @param categoryName Название категории
     */
    public CreatingExistingUserCategoryException(String message, String categoryName) {
        super(message, categoryName);
    }
}
