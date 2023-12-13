package ru.naumen.personalfinancebot.repository.category.exceptions;

/**
 * Исключение, генерируемое при попытке удаления несуществующей категории
 */
public class RemovingNonExistentCategoryException extends Exception {
    /**
     * @param message Сообщение об ошибке
     */
    public RemovingNonExistentCategoryException(String message) {
        super(message);
    }
}
