package ru.naumen.personalfinancebot.repository;

import org.hibernate.Session;
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

    public TestHibernateUserRepository() {
        testHibernateTransaction = new TestHibernateTransaction(User.class);
    }

    /**
     * Очистка всех данных в таблице с пользователями
     */
    public void removeAll(Session session) {
        testHibernateTransaction.removeAll(session);
    }
}
