package ru.naumen.personalfinancebot.repository;

import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;

/**
 * Хранилище пользователей для тестов
 */
public class TestHibernateUserRepository extends HibernateUserRepository {
    /**
     * Объект, позволяющий совершать транзакции для тестовых хранилищ
     */
    private final TestHibernateTransactions testHibernateTransactions;

    public TestHibernateUserRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
        testHibernateTransactions = new TestHibernateTransactions(User.class, sessionFactory);
    }

    /**
     * Очистка всех данных в таблице с пользователями
     */
    public void removeAll() {
        testHibernateTransactions.removeAll();
    }
}
