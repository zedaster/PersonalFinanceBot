package ru.naumen.personalfinancebot.repositories.category;

import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для хранилища категорий
 */
public interface CategoryRepository {
    /**
     * Возвращает пользовательскую категорию по имени
     * @param categoryName Имя категории
     * @return Пользовательская категория
     */
    Optional<Category> getUserCategoryByName(User user, CategoryType type, String categoryName);

    /**
     * Возвращает все категории указанного типа для указанного пользователя
     *
     * @param user Пользователь
     * @param type Тип категорий
     * @return Список из запрошенных категорий
     */
    List<Category> getUserCategoriesByType(User user, CategoryType type);

    /**
     * Возвращает стандартную категорию по имени
     * @param type Тип категории
     * @param categoryName Имя категории
     * @return Стандартная категория
     */
    Optional<Category> getStandardCategoryByName(CategoryType type, String categoryName);

    /**
     * Возвращает все стандартные категории указанного типа
     *
     * @param type Тип категорий
     * @return Список из запрошенных категорий
     */
    List<Category> getStandardCategoriesByType(CategoryType type);

    /**
     * Создаёт пользовательскую категорию
     * @param categoryName Имя категории
     * @param type Тип категории: расход / доход
     * @param user Пользователь
     * @return Созданная категория
     * @throws CreatingExistingUserCategoryException
     * если пользовательская категория с таким типом и именем для этого юзера уже существует
     * @throws CreatingExistingStandardCategoryException
     * если существует стандартная категория с таким же названием
     */
    Category createUserCategory(User user, CategoryType type, String categoryName)
            throws CreatingExistingUserCategoryException, CreatingExistingStandardCategoryException;

    /**
     * Cоздёт стандартную категорию, не относящуюся к пользователя
     * @param categoryName Имя категории
     * @param type Тип категории: расход / доход
     * @return Созданная категория
     * @throws CreatingExistingStandardCategoryException
     *  если стандартная категория с таким типом и именем уже существует
     */
    Category createStandardCategory(CategoryType type, String categoryName)
            throws CreatingExistingStandardCategoryException;

    /**
     * Удаляет категорию по ID.
     * Удаление стандартных категорий технически невозможно
     * @param id ID категории
     * @throws RemovingStandardCategoryException если категория является стандартной
     */
    void removeCategoryById(Long id) throws RemovingStandardCategoryException;

    /**
     * Удаляет пользовательскую категорию по названию
     *
     * @throws RemovingNonExistentCategoryException если такая категория не существует
     */
    void removeUserCategoryByName(User user, CategoryType type, String categoryName) throws
            RemovingNonExistentCategoryException;

    /**
     * Исключение, генерируемое при попытке создания пользовательской категории, если она уже
     * существует как пользовательская с таким же названием.
     */
    class CreatingExistingUserCategoryException extends CreatingExistingCategoryException {
        public CreatingExistingUserCategoryException(String categoryName) {
            super(categoryName);
        }
    }

    /**
     * Исключение, генерируемое при попытке создания стандартной или пользовательской категории, если уже есть
     * стандартная категория с таким же названием.
     */
    class CreatingExistingStandardCategoryException extends CreatingExistingCategoryException {
        public CreatingExistingStandardCategoryException(String categoryName) {
            super(categoryName);
        }
    }

    /**
     * Абстрация для иссключений, связанных с созданием категории, которая уже существует
     */
    abstract class CreatingExistingCategoryException extends Exception {
        /**
         * Название категории, где произошло исключение.
         */
        private final String categoryName;

        public CreatingExistingCategoryException(String categoryName) {
            this.categoryName = categoryName;
        }

        /**
         * Получает название категории, где произошло исключение.
         */
        public String getCategoryName() {
            return categoryName;
        }
    }

    /**
     * Метод возвращает собственную категорию пользователя, либо стандартную.
     * @param user Пользователь
     * @param categoryName Название категории
     * @param type Тип категории
     * @return Категория / null
     */
    Category getCategoryByName(User user, String categoryName, CategoryType type) throws CategoryNotExistsException;

    /**
     * Исключение, генерируемое при попытке удаления стандартной категории
     */
    class RemovingStandardCategoryException extends Exception {

    }

    /**
     * Исключение, генерируемое при попытке удаления несуществующей категории
     */
    class RemovingNonExistentCategoryException extends Exception {

    }

    /**
     * Исключение, генерируемое при попытке добавить операцию по несуществеющей категории
     */
    class CategoryNotExistsException extends Exception {
        public CategoryNotExistsException(String message) {
            super(message);
        }

        public CategoryNotExistsException(){}
    }
}
