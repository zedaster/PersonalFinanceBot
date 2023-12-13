package ru.naumen.personalfinancebot.repository.category.exception;

/**
 * Исключение, генерируемое при попытке создания стандартной или пользовательской категории, если уже есть
 * стандартная категория с таким же названием.
 */
public class CreatingExistingStandardCategoryException extends CreatingExistingCategoryException {
    /**
     * @param categoryName Название категории
     */
    public CreatingExistingStandardCategoryException(String categoryName) {
        super(String.format("Уже существует стандартная категория с названием '%s'", categoryName), categoryName);
    }
}