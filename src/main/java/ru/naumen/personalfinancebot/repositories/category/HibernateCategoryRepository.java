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
     *
     * @param user         Пользователь
     * @param categoryName Имя категории
     * @param type         Тип категории: расход / доход
     * @return Категория
     * @throws CreatingExistingUserCategoryException     если пользовательская категория с таким типом и именем для этого юзера уже существует
     * @throws CreatingExistingStandardCategoryException если существует стандартная категория с таким же названием
     */
    @Override
    public Category createUserCategory(User user, CategoryType type, String categoryName) throws
            CreatingExistingStandardCategoryException, CreatingExistingUserCategoryException {
        Optional<Category> existingUserCategory = this.getCategoryByName(user, type, categoryName);

        if (existingUserCategory.isPresent()) {
            if (existingUserCategory.get().isStandard()) {
                throw new CreatingExistingStandardCategoryException(categoryName);
            } else {
                throw new CreatingExistingUserCategoryException(categoryName);
            }
        }

        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setType(type);
        category.setUser(user);
        return createCategory(category);
    }

    /**
     * Создает стандартную категорию, не относящуюся к пользователю.
     *
     * @param categoryName Имя категории
     * @param type         Тип категории
     * @return Категория
     * @throws CreatingExistingStandardCategoryException если стандартная категория с таким типом и именем уже существует
     */
    @Override
    public Category createStandardCategory(CategoryType type, String categoryName)
            throws CreatingExistingStandardCategoryException {
        if (this.getStandardCategoryByName(type, categoryName).isPresent()) {
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
            if (category == null) {
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
     * Удаляет пользовательскую категорию по названию
     *
     * @param categoryName - название категории
     * @throws RemovingNonExistentCategoryException если такая персональная категория не существует
     */
    public void removeUserCategoryByName(User user, CategoryType type, String categoryName)
            throws RemovingNonExistentCategoryException {
        try (Session session = sessionFactory.openSession()) {
            Optional<Category> category = getCategoryByName(user, type, categoryName);
            if (category.isEmpty() || category.get().isStandard()) {
                throw new RemovingNonExistentCategoryException();
            }

            session.beginTransaction();
            session.delete(category.get());
            session.getTransaction().commit();
        }
    }

    /**
     * Метод возвращает либо собственную категорию пользователя, либо стандартную.
     *
     * @param user         Пользователь или null (при null доступны только стандартные категории)
     * @param categoryName Название категории
     * @param type         Тип категории
     * @return Опциональный объект категории (пуст, если категория не найдена)
     */
    @Override
    public Optional<Category> getCategoryByName(@Nullable User user, CategoryType type, String categoryName) {
        try (Session session = sessionFactory.openSession()) {
            Query<Category> resultQuery;
            if (user == null) {
                resultQuery = selectCategoriesSeparately(session, type, null, categoryName);
            } else {
                resultQuery = selectCategoriesTogether(session, type, user, categoryName);
            }

            return resultQuery
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
            return selectCategoriesSeparately(session, type, user, null).getResultList();
        }
    }

    /**
     * Делает запрос категорий в БД. Возвращает запрос, содержащий <b>или</b> стандартные категории при user == null,
     * <b>или</b> персональные категории в ином случае.
     * Регистр названия категории при выборке игнорируется.
     */
    private Query<Category> selectCategoriesSeparately(Session session, CategoryType type, @Nullable User user,
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
     * Делает запрос категорий в БД. Возвращает запрос, содержащий <b>и</b> стандартные категории,
     * <b>и</b> персональные категории.
     * Регистр названия категории при выборке игнорируется.
     */
    private Query<Category> selectCategoriesTogether(Session session, CategoryType type, User user, String categoryName) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Category> cq = cb.createQuery(Category.class);
        Root<Category> root = cq.from(Category.class);

        cq.select(root).where(cb.and(
                cb.or(
                        cb.equal(root.get("user"), user.getId()), // userId == userId
                        cb.isNull(root.get("user")) // userId is null
                ),
                cb.equal(root.get("type"), type),
                cb.equal(cb.lower(root.get("categoryName")), categoryName.toLowerCase())
        ));

        return session.createQuery(cq);
    }

    /**
     * Делегирующий метод для создания записи категории в базе данных
     *
     * @param category Категория
     * @return Категория
     */
    private Category createCategory(Category category) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(category);
            session.getTransaction().commit();
            return category;
        }
    }
}
