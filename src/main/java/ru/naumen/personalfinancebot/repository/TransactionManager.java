package ru.naumen.personalfinancebot.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.function.Consumer;

/**
 * Базовый класс для репозитория, работающего на Hibernate
 */
public class TransactionManager {
    /**
     * Фабрика для открытия сессий
     */
    private final SessionFactory sessionFactory;

    public TransactionManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Открывает сессию на время лямбды и производит Hibernate транзакцию правильным образом (открытие, затем коммит
     * или откат при исключении), после которой возвращается какое-либо значение
     *
     * @param consumer лямбда-функция, в которой можно произвести действия во время открытой сессии и транзакции для
     * этой сессии и вернуть необходимое значение.
     */
    public void produceTransaction(Consumer<Session> consumer) {
        try (Session session = sessionFactory.getCurrentSession()) {
            final Transaction transaction = session.beginTransaction();
            try {
                consumer.accept(session);
                transaction.commit();
            } catch (final Exception e) {
                transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
