package ru.naumen.personalfinancebot.repositories.category;

import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.User;

import java.util.Optional;

public interface CategoryRepository {
    /**
     * Возвращает категорию по имени
     * @param categoryName Имя категории
     * @return Категория
     */
    Optional<Category> getCategoryByName(String categoryName);

    /**
     * Создаёт категория, которую добавил пользователь
     * @param categoryName Имя категории
     * @param type Тип категории: расход / доход
     * @param user Пользователь
     * @return Категория
     */
    Category createCategory(String categoryName, String type, User user);

    /**
     * Cоздёт стандартную категорию, не относящуюся к пользователя
     * @param categoryName
     * @param type
     * @return
     */
    Category createStandartCategory(String categoryName, String type);

    /**
     * Удаляет категорию по ID
     * @param id ID категории
     */
    void deleteCategoryById(Long id);

    /**
     * Удаляет категорию по названию
     */
    void deleteCategoryByName(String categoryName);
}
