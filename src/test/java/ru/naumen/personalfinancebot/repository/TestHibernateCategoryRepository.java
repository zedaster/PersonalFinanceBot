package ru.naumen.personalfinancebot.repository;

import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.repository.category.HibernateCategoryRepository;

/**
 * Хранилище категорий для тестов
 */
public class TestHibernateCategoryRepository extends HibernateCategoryRepository {
    /**
     * Объект, позволяющий совершать транзакции для тестовых хранилищ
     */
    private final TestHibernateTransaction testHibernateTransaction;

    public TestHibernateCategoryRepository(SessionFactory sessionFactory) {
        testHibernateTransaction = new TestHibernateTransaction(Category.class, sessionFactory);
    }

    /**
     * Очистка всех данных в таблице с категориями
     */
    public void removeAll() {
        testHibernateTransaction.removeAll();
    }
}
