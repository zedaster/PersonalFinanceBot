package ru.naumen.personalfinancebot.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;

import javax.persistence.Query;

public class TestHibernateCategoryRepository extends HibernateCategoryRepository {
    public TestHibernateCategoryRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public void removeAll() {
        try (Session session = sessionFactory.openSession()) {
            String hql = "delete from " + Category.class.getName();
            session.beginTransaction();
            Query query = session.createQuery(hql);
            query.executeUpdate();
            session.getTransaction().commit();
        }
    }
}
