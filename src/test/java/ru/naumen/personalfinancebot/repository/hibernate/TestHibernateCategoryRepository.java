package ru.naumen.personalfinancebot.repository.hibernate;

import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;

/**
 * Хранилище категорий для тестов
 */
public class TestHibernateCategoryRepository extends HibernateCategoryRepository {
    /**
     * Объект, позволяющий совершать транзакции для тестовых хранилищ
     */
    private final TestHibernateTransactions testHibernateTransactions;

    public TestHibernateCategoryRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
        testHibernateTransactions = new TestHibernateTransactions(Category.class, sessionFactory);
    }

    /**
     * Очистка всех данных в таблице с категориями
     */
    public void removeAll() {
        testHibernateTransactions.removeAll();
    }
}
