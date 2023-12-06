package ru.naumen.personalfinancebot.repositories;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Базовый класс для репозитория, работающего на Hibernate
 */
public abstract class HibernateRepository {
    /**
     * Фабрика для открытия сессий
     */
    private final SessionFactory sessionFactory;

    public HibernateRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Открывает сессию на время лямбды и производит Hibernate транзакцию правильным образом (открытие, затем коммит
     * или откат при исключении), после которой возвращается какое-либо значение
     *
     * @param function лямбда-функция, в которой можно произвести действия во время открытой сессии и транзакции для
     *                 этой сессии и вернуть необходимое значение.
     * @param <T>      Тип возвращаемого значения для function и этого метода соотвественно
     * @return значение, которое вернулось в function
     */
    protected <T> T produceTransaction(Function<Session, T> function) {
        try (Session session = sessionFactory.getCurrentSession()) {
            final Transaction transaction = session.beginTransaction();
            try {
                final T result = function.apply(session);
                transaction.commit();
                return result;
            } catch (final Exception e) {
                transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Открывает сессию на время лямбды и производит Hibernate транзакцию правильным образом (открытие, затем коммит
     * или откат при исключении), после которой не возвращается какое-либо значение
     *
     * @param consumer лямбда-функция, в которой можно произвести действия во время открытой сессии и транзакции для
     *                 этой сессии.
     */
    protected void produceVoidTransaction(Consumer<Session> consumer) {
        produceTransaction((session -> {
            consumer.accept(session);
            return null;
        }));
    }
}
