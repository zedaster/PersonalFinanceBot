package ru.naumen.personalfinancebot.repository.category.exception;

/**
 * Исключение, генерируемое при попытке удаления несуществующей категории
 */
public class RemovingNonExistentCategoryException extends Exception {
    /**
     * @param categoryName Название категории
     */
    public RemovingNonExistentCategoryException(String categoryName) {
        super(String.format("Невозможно удалить категорию '%s', так как ее не существует!", categoryName));
    }
}
