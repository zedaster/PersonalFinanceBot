package ru.naumen.personalfinancebot.repository.category;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.hibernate.Session;
import org.hibernate.query.Query;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingStandardCategoryException;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingUserCategoryException;
import ru.naumen.personalfinancebot.repository.category.exception.NotExistingCategoryException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Хранилище категорий с использованием Hibernate
 */
public class HibernateCategoryRepository implements CategoryRepository {

    @Override
    public List<Category> getUserCategoriesByType(Session session, @NotNull User user, CategoryType type) {
        return getCategoriesByType(session, user, type);
    }

    @Override
    public List<Category> getStandardCategoriesByType(Session session, CategoryType type) {
        return getCategoriesByType(session, null, type);
    }

    @Override
    public Category createUserCategory(Session session, User user, CategoryType type, String categoryName) throws
            ExistingStandardCategoryException, ExistingUserCategoryException {
        Optional<Category> existingUserCategory = this.getCategoryByName(session, user, type, categoryName);

        if (existingUserCategory.isPresent()) {
            if (existingUserCategory.get().isStandard()) {
                throw new ExistingStandardCategoryException(categoryName);
            } else {
                throw new ExistingUserCategoryException(categoryName);
            }
        }

        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setType(type);
        category.setUser(user);
        return createCategory(session, category);
    }

    @Override
    public Category createStandardCategory(Session session, CategoryType type, String categoryName)
            throws ExistingStandardCategoryException {
        if (this.getStandardCategoryByName(session, type, categoryName).isPresent()) {
            throw new ExistingStandardCategoryException(categoryName);
        }

        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setType(type);
        return createCategory(session, category);
    }

    public void removeUserCategoryByName(Session session, User user, CategoryType type, String categoryName)
            throws NotExistingCategoryException {
        Optional<Category> category = getCategoryByName(session, user, type, categoryName);
        if (category.isEmpty() || category.get().isStandard()) {
            throw new NotExistingCategoryException(categoryName);
        }
        session.delete(category.get());
    }

    @Override
    public Optional<Category> getCategoryByName(Session session, @Nullable User user, CategoryType type, String categoryName) {
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

    /**
     * Получает либо пользовательские, либо стандартные (при user = null) категории определенного типа.
     * Регистр названия категории игнорируется.
     */
    private List<Category> getCategoriesByType(Session session, @Nullable User user, CategoryType type) {
        Query<Category> query = selectCategoriesSeparately(session, type, user, null);
        return query.getResultList();
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
    private Category createCategory(Session session, Category category) {
        session.save(category);
        return category;
    }
}
