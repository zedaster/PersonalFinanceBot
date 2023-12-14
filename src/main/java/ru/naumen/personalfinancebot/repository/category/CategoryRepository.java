package ru.naumen.personalfinancebot.repository.category;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingStandardCategoryException;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingUserCategoryException;
import ru.naumen.personalfinancebot.repository.category.exception.NotExistingCategoryException;

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
     * @throws ExistingUserCategoryException     если пользовательская категория с таким типом и именем для этого юзера уже существует
     * @throws ExistingStandardCategoryException если существует стандартная категория с таким же названием
     */
    Category createUserCategory(Session session, User user, CategoryType type, String categoryName)
            throws ExistingUserCategoryException, ExistingStandardCategoryException;

    /**
     * Создаёт стандартную категорию, не относящуюся к пользователя
     *
     * @param categoryName Имя категории
     * @param type         Тип категории: расход / доход
     * @throws ExistingStandardCategoryException если стандартная категория с таким типом и именем уже существует
     * @return Созданная категория
     */
    Category createStandardCategory(Session session, CategoryType type, String categoryName)
            throws ExistingStandardCategoryException;

    /**
     * Удаляет пользовательскую категорию по названию
     *
     * @throws NotExistingCategoryException если такая категория не существует
     */
    void removeUserCategoryByName(Session session, User user, CategoryType type, String categoryName)
            throws NotExistingCategoryException;

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
