package ru.naumen.personalfinancebot.repository.category.exception;

/**
 * Исключение, генерируемое при попытке удаления стандартной категории
 */
public class RemovingStandardCategoryException extends Exception {

    /**
     * @param categoryName Название категории
     */
    public RemovingStandardCategoryException(String categoryName) {
        super(String.format("Невозможно удалить категорию '%s', так как она является стандартной!", categoryName));
    }
}
