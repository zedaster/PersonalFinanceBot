package ru.naumen.personalfinancebot.repository.hibernate;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;

public class TestHibernateOperationRepository extends HibernateOperationRepository {
    /**
     * Объект, позволяющий совершать транзакции для тестовых хранилищ
     */
    private final TestHibernateTransaction testHibernateTransaction;

    public TestHibernateOperationRepository() {
        testHibernateTransaction = new TestHibernateTransaction(Operation.class);
    }

    /**
     * Очистка всех данных в таблице с категориями
     */
    public void removeAll(Session session) {
        testHibernateTransaction.removeAll(session);
    }
}
