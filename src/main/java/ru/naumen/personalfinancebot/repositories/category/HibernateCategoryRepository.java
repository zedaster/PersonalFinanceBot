package ru.naumen.personalfinancebot.repositories.category;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Optional;

public class HibernateCategoryRepository implements CategoryRepository {
    private final SessionFactory sessionFactory;

    public HibernateCategoryRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Возвращает категорию по имени
     * @param categoryName Имя категории
     * @return Категория
     */
    @Override
    public Optional<Category> getCategoryByName(String categoryName) {
        try (Session session = sessionFactory.openSession()){
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Category> criteriaQuery = criteriaBuilder.createQuery(Category.class);
            criteriaQuery.
                    select(criteriaQuery.from(Category.class)).
                    where(criteriaBuilder.equal(criteriaQuery
                            .from(Category.class)
                            .get("categoryName"),
                            categoryName)
                    );
            return Optional.ofNullable(session.createQuery(criteriaQuery).uniqueResult());
        }
    }

    /**
     * Создаёт категория, которую добавил пользователь
     * @param categoryName Имя категории
     * @param type Тип категории: расход / доход
     * @param user Пользователь
     * @return Категория
     */
    @Override
    public Category createCategory(String categoryName, String type, User user) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setType(type);
        category.setUser(user);
        return createCategory(category);
    }

    /**
     * Cоздёт стандартную категорию, не относящуюся к пользователя
     * @param categoryName Имя категории
     * @param type Тип категории
     * @return Категория
     */
    @Override
    public Category createStandartCategory(String categoryName, String type) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setType(type);
        return createCategory(category);
    }

    /**
     * Удаляет категорию по ID
     *
     * @param id ID категории
     */
    @Override
    public void deleteCategoryById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Optional<Category> category = Optional.ofNullable(session.get(Category.class, id));
            if (category.isEmpty()){
                return;
            }
            session.beginTransaction();
            session.delete(category.get());
            session.getTransaction().commit();
            session.close();
        }
    }

    /**
     * Удаляет категорию по названию
     * @param categoryName - название категории
     */
    @Override
    public void deleteCategoryByName(String categoryName) {
        try (Session session = sessionFactory.openSession()) {
            Optional<Category> category = getCategoryByName(categoryName);
            if (category.isEmpty()){
                return;
            }
            session.beginTransaction();
            session.delete(category.get());
            session.getTransaction().commit();
            session.close();
        }
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
            session.close();
            return category;
        }
    }
}
