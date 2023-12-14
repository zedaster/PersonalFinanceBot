package ru.naumen.personalfinancebot.repository;

import org.hibernate.Session;
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

    public TestHibernateCategoryRepository() {
        testHibernateTransaction = new TestHibernateTransaction(Category.class);
    }

    /**
     * Очистка всех данных в таблице с категориями
     */
    public void removeAll(Session session) {
        testHibernateTransaction.removeAll(session);
    }
}
