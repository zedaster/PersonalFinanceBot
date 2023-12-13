package ru.naumen.personalfinancebot.repository.category.exception;

/**
 * Исключение, генерируемое при попытке создания пользовательской категории, если она уже
 * существует как пользовательская с таким же названием.
 */
public class CreatingExistingUserCategoryException extends CreatingExistingCategoryException {
    /**
     * @param categoryName Название категории
     */
    public CreatingExistingUserCategoryException(String categoryName) {
        super(String.format("Уже существует пользовательская категория с названием '%s'", categoryName), categoryName);
    }
}
