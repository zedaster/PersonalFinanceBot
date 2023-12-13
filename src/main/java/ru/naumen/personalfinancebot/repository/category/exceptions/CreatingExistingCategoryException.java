package ru.naumen.personalfinancebot.repository.category.exceptions;

/**
 * Абстрация для иссключений, связанных с созданием категории, которая уже существует
 */
public class CreatingExistingCategoryException extends Exception {
    /**
     * Название категории, где произошло исключение.
     */
    private final String categoryName;

    /**
     * @param message Сообщение ооб ошибке
     * @param categoryName Название категории
     */
    CreatingExistingCategoryException(String message, String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * @return Название категории
     */
    public String getCategoryName() {
        return this.categoryName;
    }
}
