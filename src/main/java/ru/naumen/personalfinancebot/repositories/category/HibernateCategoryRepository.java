package ru.naumen.personalfinancebot.repositories.category;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HibernateCategoryRepository implements CategoryRepository {
    protected final SessionFactory sessionFactory;

    public HibernateCategoryRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Возвращает категорию по имени. Регистр названия категории игнорируется.
     * @param categoryName Имя категории
     * @return Категория
     */
    @Override
    public Optional<Category> getUserCategoryByName(@NotNull User user, CategoryType type, String categoryName) {
        if (user == null) throw new IllegalArgumentException();
        return getCategory(user, type, categoryName);
    }

    /**
     * Возвращает все категории указанного типа для указанного пользователя.
     *
     * @param user Пользователь
     * @param type Тип категорий
     * @return Список из запрошенных категорий
     */
    @Override
    public List<Category> getUserCategoriesByType(@NotNull User user, CategoryType type) {
        if (user == null) throw new IllegalArgumentException();
        return getCategoriesByType(user, type);
    }

    /**
     * Возвращает стандартную категорию по имени. Регистр названия категории игнорируется.
     *
     * @param type         Тип категории
     * @param categoryName Имя категории
     * @return Стандартная категория
     */
    @Override
    public Optional<Category> getStandardCategoryByName(CategoryType type, String categoryName) {
        return getCategory(null, type, categoryName);
    }

    /**
     * Возвращает все стандартные категории указанного типа
     *
     * @param type Тип категорий
     * @return Список из запрошенных категорий
     */
    @Override
    public List<Category> getStandardCategoriesByType(CategoryType type) {
        return getCategoriesByType(null, type);
    }

    /**
     * Создаёт категорию, которую добавил пользователь
     * @param user Пользователь
     * @param categoryName Имя категории
     * @param type Тип категории: расход / доход
     * @return Категория
     * @throws CreatingExistingUserCategoryException
     * если пользовательская категория с таким типом и именем для этого юзера уже существует
     * @throws CreatingExistingStandardCategoryException
     * если существует стандартная категория с таким же названием
     */
    @Override
    public Category createUserCategory(User user, CategoryType type, String categoryName) throws
            CreatingExistingStandardCategoryException, CreatingExistingUserCategoryException {
        Optional<Category> existingUserCategory = this.getUserCategoryByName(user, type, categoryName);
        if (existingUserCategory.isPresent()) {
            throw new CreatingExistingUserCategoryException(categoryName);
        }

        Optional<Category> existingStandardCategory = this.getStandardCategoryByName(type, categoryName);
        if (existingStandardCategory.isPresent()) {
            throw new CreatingExistingStandardCategoryException(categoryName);
        }

        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setType(type);
        category.setUser(user);
        return createCategory(category);
    }

    /**
     * Создает стандартную категорию, не относящуюся к пользователю.
     * @param categoryName Имя категории
     * @param type Тип категории
     * @return Категория
     * @throws CreatingExistingStandardCategoryException
     * если стандартная категория с таким типом и именем уже существует
     */
    @Override
    public Category createStandardCategory(CategoryType type, String categoryName)
            throws CreatingExistingStandardCategoryException {
        Optional<Category> existingCategory = this.getStandardCategoryByName(type, categoryName);
        if (existingCategory.isPresent()) {
            throw new CreatingExistingStandardCategoryException(categoryName);
        }

        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setType(type);
        return createCategory(category);
    }

    /**
     * Удаляет категорию по ID
     *
     * @param id ID категории
     * @throws RemovingStandardCategoryException если категория является стандартной
     */
    @Override
    public void removeCategoryById(Long id) throws RemovingStandardCategoryException {
        try (Session session = sessionFactory.openSession()) {
            Category category = session.get(Category.class, id);
            if (category == null){
                return;
            }
            if (category.isStandard()) {
                throw new RemovingStandardCategoryException();
            }
            session.beginTransaction();
            session.delete(category);
            session.getTransaction().commit();
        }
    }

    /**
     * Удаляет категорию по названию
     * @param categoryName - название категории
     * @throws RemovingNonExistentCategoryException если такая категория не существует
     */
    public void removeUserCategoryByName(User user, CategoryType type, String categoryName)
            throws RemovingNonExistentCategoryException {
        try (Session session = sessionFactory.openSession()) {
            Optional<Category> category = getUserCategoryByName(user, type, categoryName);
            if (category.isEmpty()) {
                throw new RemovingNonExistentCategoryException();
            }

            session.beginTransaction();
            session.delete(category.get());
            session.getTransaction().commit();
        }
    }

    /**
     * Получает категорию либо пользовательскую, либо стандартную, если user = null.
     * Регистр названия категории игнорируется.
     */
    private Optional<Category> getCategory(@Nullable User user, CategoryType type, String categoryName) {
        try (Session session = sessionFactory.openSession()) {
            return createSelectCategoriesQuery(session, type, user, categoryName)
                    .getResultStream()
                    .findFirst();
        }
    }

    /**
     * Получает либо пользовательские, либо стандартные (при user = null) категории определенного типа.
     * Регистр названия категории игнорируется.
     */
    private List<Category> getCategoriesByType(@Nullable User user, CategoryType type) {
        try (Session session = sessionFactory.openSession()) {
            return createSelectCategoriesQuery(session, type, user, null).getResultList();
        }
    }

    /**
     * Делает запрос категорий в БД.
     * Регистр названия категории при выборке игнорируется.
     */
    private Query<Category> createSelectCategoriesQuery(Session session, CategoryType type, @Nullable User user,
                                                        @Nullable String categoryName) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Category> cq = cb.createQuery(Category.class);
        Root<Category> root = cq.from(Category.class);

        List<Predicate> selectPredicates = new ArrayList<>();

        Predicate userIdEquity;
        if (user != null) {
            userIdEquity = cb.equal(root.get("user"), user.getId());
        } else {
            userIdEquity = cb.isNull(root.get("user"));
        }
        selectPredicates.add(userIdEquity);

        Predicate categoryTypeEquity = cb.equal(root.get("type"), type);
        selectPredicates.add(categoryTypeEquity);

        if (categoryName != null) {
            Predicate categoryNameEquity = cb.equal(cb.lower(root.get("categoryName")), categoryName.toLowerCase());
            selectPredicates.add(categoryNameEquity);
        }

        Predicate[] selectPredicatesArray = selectPredicates.toArray(new Predicate[0]);
        cq.select(root).where(cb.and(selectPredicatesArray));
        return session.createQuery(cq);
    }

    /**
     * Делегирующий метод для создания записи категории в базе данных
     * @param category Категория
     * @return Категория
     */
    private Category createCategory(Category category){
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(category);
            session.getTransaction().commit();
            return category;
        }
    }
}
