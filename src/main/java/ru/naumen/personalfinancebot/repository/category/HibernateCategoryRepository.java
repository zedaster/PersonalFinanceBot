package ru.naumen.personalfinancebot.repository.category;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.HibernateRepository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HibernateCategoryRepository extends HibernateRepository implements CategoryRepository {
    public HibernateCategoryRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
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
        boolean isRemovingSuccessful = produceTransaction(session -> {
            Category category = session.get(Category.class, id);
            if (category == null) {
                return true;
            }
            if (category.isStandard()) {
                return false;
            }
            session.delete(category);
            return true;
        });

        if (!isRemovingSuccessful) {
            throw new RemovingStandardCategoryException();
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
        Optional<Category> category = getCategoryByName(user, type, categoryName);
        if (category.isEmpty() || category.get().isStandard()) {
            throw new RemovingNonExistentCategoryException();
        }

        produceVoidTransaction(session -> session.delete(category.get()));
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
        return produceTransaction(session -> {
            Query<Category> resultQuery;

            if (user == null) {
                resultQuery = selectCategoriesSeparately(session, type, null, categoryName);
            } else {
                resultQuery = selectCategoriesTogether(session, type, user, categoryName);
            }

            return resultQuery
                    .getResultStream()
                    .findFirst();
        });
    }

    /**
     * Получает либо пользовательские, либо стандартные (при user = null) категории определенного типа.
     * Регистр названия категории игнорируется.
     */
    private List<Category> getCategoriesByType(@Nullable User user, CategoryType type) {
        return produceTransaction(session -> {
            Query<Category> query = selectCategoriesSeparately(session, type, user, null);
            return query.getResultList();
        });
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
        return produceTransaction(session -> {
            session.save(category);
            return category;
        });
    }
}
