package ru.naumen.personalfinancebot.repository;

import org.hibernate.Session;

import javax.persistence.Query;
import javax.persistence.Table;

/**
 * Класс, позволяющий совершать Hibernate транзакции для очищения хранилищ
 */
public class ClearQueryManager {
    /**
     * Создает экземпляр класса, позволяющий совершать Hibernate транзакции для тестовых хранилищ
     */
    public ClearQueryManager() {
    }

    /**
     * Очищает все данные в таблице
     * @param session Сессия
     * @param hibernateModelClasses Hibernate таблицы, которые будут очищены
     */
    public void clear(Session session, Class<?>... hibernateModelClasses) {
        for (Class<?> model : hibernateModelClasses) {
            if (!model.isAnnotationPresent(Table.class)) {
                throw new IllegalArgumentException("Class '%s' doesn't have table annotation!".formatted(model.getName()));
            }
            String hql = "delete from " + model.getName();
            Query query = session.createQuery(hql);
            query.executeUpdate();
        }
    }
}
