package ru.naumen.personalfinancebot.repository.hibernate;

import org.hibernate.Session;

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
     * Создает экземпляр класса, позволяющий совершать Hibernate транзакции для тестовых хранилищ
     *
     * @param hibernateModelClass Hibernate модель (Класс обязательно должен иметь аннотацию Table)
     */
    public TestHibernateTransaction(Class<?> hibernateModelClass) {
        if (!hibernateModelClass.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The hibernate model must has table annotation!");
        }
        this.hibernateModelClass = hibernateModelClass;
    }

    public void removeAll(Session session) {
        String hql = "delete from " + hibernateModelClass.getName();
        Query query = session.createQuery(hql);
        query.executeUpdate();
    }
}
