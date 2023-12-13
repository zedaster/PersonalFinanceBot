package ru.naumen.personalfinancebot.repository.category.exceptions;

/**
 * Исключение, выбрасываемое в случае, если категория не существует
 */
public class CategoryDoesNotExist extends Exception {
    /**
     * @param message Сообщение об ошибке
     */
    public CategoryDoesNotExist(String message) {
        super(message);
    }
}
