package ru.naumen.personalfinancebot.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import ru.naumen.personalfinancebot.models.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

/**
 * Реализация хранилища пользователей в БД с помощью библиотеки Hibernate
 */
// TODO
public class HibernateUserRepository implements UserRepository {
    /**
     * Для открытия новых сессий
     */
    private final SessionFactory sessionFactory;

    public HibernateUserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Получает пользователя из БД по chat id из telegram.
     */
    @Override
    public Optional<User> getUserByTelegramChatId(Long chatId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);
            cq.select(root).where(cb.equal(root.get("chat_id"), chatId));

            Query<User> query = session.createQuery(cq);
            List<User> users = query.getResultList();

            if (!users.isEmpty()) {
                return Optional.of(users.get(0));
            }
        }
        return Optional.empty();
    }

    /**
     * Сохраняет пользователя в БД
     */
    @Override
    public void saveUser(User user) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            if (user.getId() == null) {
                session.persist(user);
            } else {
                session.merge(user);
            }
            session.getTransaction().commit();
        }
    }

    /**
     * Удаляет пользователя с таким id из БД
     */
    @Override
    public void removeUserById(long id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
            }
            session.getTransaction().commit();
        }
    }
}
