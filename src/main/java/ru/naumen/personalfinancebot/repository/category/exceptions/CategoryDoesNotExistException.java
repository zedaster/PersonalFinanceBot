package ru.naumen.personalfinancebot.repository.category.exceptions;

/**
 * Исключение, выбрасываемое в случае, если категория не существует
 */
public class CategoryDoesNotExistException extends Exception {
    /**
     * @param message Сообщение об ошибке
     */
    public CategoryDoesNotExistException(String message) {
        super(message);
    }
}
