package ru.naumen.personalfinancebot.repository.user;

import org.hibernate.Session;
import org.hibernate.query.Query;
import ru.naumen.personalfinancebot.model.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Optional;

/**
 * Реализация хранилища пользователей в БД с помощью библиотеки Hibernate
 */
public class HibernateUserRepository implements UserRepository {

    /**
     * Получает пользователя из БД по chat id из telegram.
     */
    @Override
    public Optional<User> getUserByTelegramChatId(Session session, Long chatId) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.select(root).where(cb.equal(root.get("chatId"), chatId));

        Query<User> query = session.createQuery(cq);
        return query.getResultStream().findFirst();
    }

    /**
     * Сохраняет пользователя в БД
     */
    @Override
    public void saveUser(Session session, User user) {
        session.saveOrUpdate(user);
    }

    /**
     * Удаляет пользователя с таким id из БД
     */
    @Override
    public void removeUserById(Session session, long id) {
        User user = session.get(User.class, id);
        if (user != null) {
            session.delete(user);
        }
    }
}
