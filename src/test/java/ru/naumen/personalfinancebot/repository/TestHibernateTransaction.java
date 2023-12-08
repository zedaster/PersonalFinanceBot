package ru.naumen.personalfinancebot.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.Query;
import javax.persistence.Table;

/**
 * Класс, позволяющий совершать Hibernate транзакции для тестовых хранилищ
 */
public class TestHibernateTransaction {
    /**
     * Класс модели
     */
    private final Class<?> hibernateModelClass;

    /**
     * SessionFactory для отрытия и закрытия сессий
     */
    private final SessionFactory sessionFactory;

    /**
     * Создает экземпляр класса, позволяющий совершать Hibernate транзакции для тестовых хранилищ
     *
     * @param hibernateModelClass Hibernate модель (Класс обязательно должен иметь аннотацию Table)
     * @param sessionFactory      SessionFactory для отрытия и закрытия сессий
     */
    public TestHibernateTransaction(Class<?> hibernateModelClass, SessionFactory sessionFactory) {
        if (!hibernateModelClass.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The hibernate model must has table annotation!");
        }
        this.hibernateModelClass = hibernateModelClass;
        this.sessionFactory = sessionFactory;
    }

    /**
     * Удаляет все данные из таблицы
     */
    public void removeAll() {
        try (Session session = sessionFactory.openSession()) {
            String hql = "delete from " + hibernateModelClass.getName();
            session.beginTransaction();
            Query query = session.createQuery(hql);
            query.executeUpdate();
            session.getTransaction().commit();
        }
    }
}
