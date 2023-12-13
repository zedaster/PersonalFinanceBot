package ru.naumen.personalfinancebot.repository.category;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.category.exceptions.*;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для хранилища категорий
 */
public interface CategoryRepository {
    /**
     * Возвращает все категории указанного типа для указанного пользователя
     *
     * @param user Пользователь
     * @param type Тип категорий
     * @return Список из запрошенных категорий
     */
    List<Category> getUserCategoriesByType(Session session, User user, CategoryType type);

    /**
     * Возвращает стандартную категорию по имени
     *
     * @param type         Тип категории
     * @param categoryName Имя категории
     * @return Стандартная категория
     */
    default Optional<Category> getStandardCategoryByName(Session session, CategoryType type, String categoryName) {
        return getCategoryByName(session, null, type, categoryName);
    }

    /**
     * Возвращает все стандартные категории указанного типа
     *
     * @param type Тип категорий
     * @return Список из запрошенных категорий
     */
    List<Category> getStandardCategoriesByType(Session session, CategoryType type);

    /**
     * Создаёт пользовательскую категорию
     *
     * @param categoryName Имя категории
     * @param type         Тип категории: расход / доход
     * @param user         Пользователь
     * @return Созданная категория
     * @throws CreatingExistingUserCategoryException     если пользовательская категория с таким типом и именем для этого юзера уже существует
     * @throws CreatingExistingStandardCategoryException если существует стандартная категория с таким же названием
     */
    Category createUserCategory(Session session, User user, CategoryType type, String categoryName)
            throws CreatingExistingUserCategoryException, CreatingExistingStandardCategoryException;

    /**
     * Cоздёт стандартную категорию, не относящуюся к пользователя
     *
     * @param categoryName Имя категории
     * @param type         Тип категории: расход / доход
     * @return Созданная категория
     * @throws CreatingExistingStandardCategoryException если стандартная категория с таким типом и именем уже существует
     */
    Category createStandardCategory(Session session, CategoryType type, String categoryName)
            throws CreatingExistingStandardCategoryException;

    /**
     * Удаляет категорию по ID.
     * Удаление стандартных категорий технически невозможно
     *
     * @param id ID категории
     * @throws RemovingStandardCategoryException если категория является стандартной
     */
    void removeCategoryById(Session session, Long id) throws RemovingStandardCategoryException;

    /**
     * Удаляет пользовательскую категорию по названию
     *
     * @throws RemovingNonExistentCategoryException если такая категория не существует
     */
    void removeUserCategoryByName(Session session, User user, CategoryType type, String categoryName) throws RemovingNonExistentCategoryException;

    /**
     * Метод возвращает либо собственную категорию пользователя, либо стандартную.
     *
     * @param user         Пользователь
     * @param categoryName Название категории
     * @param type         Тип категории
     * @return Опциональный объект категории (пуст, если категория не найдена)
     */
    Optional<Category> getCategoryByName(Session session, User user, CategoryType type, String categoryName);
}
