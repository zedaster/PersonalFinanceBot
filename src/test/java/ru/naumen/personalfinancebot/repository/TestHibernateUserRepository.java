package ru.naumen.personalfinancebot.repository;

import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;

/**
 * Хранилище пользователей для тестов
 */
public class TestHibernateUserRepository extends HibernateUserRepository {
    /**
     * Объект, позволяющий совершать транзакции для тестовых хранилищ
     */
    private final TestHibernateTransaction testHibernateTransaction;

    public TestHibernateUserRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
        testHibernateTransaction = new TestHibernateTransaction(User.class, sessionFactory);
    }

    /**
     * Очистка всех данных в таблице с пользователями
     */
    public void removeAll() {
        testHibernateTransaction.removeAll();
    }
}
