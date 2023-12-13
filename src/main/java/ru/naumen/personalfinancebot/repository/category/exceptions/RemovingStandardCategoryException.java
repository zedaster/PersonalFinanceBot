package ru.naumen.personalfinancebot.repository.category.exceptions;

/**
 * Исключение, генерируемое при попытке удаления стандартной категории
 */
public class RemovingStandardCategoryException extends Exception {
    public RemovingStandardCategoryException(String message) {
        super(message);
    }
}
