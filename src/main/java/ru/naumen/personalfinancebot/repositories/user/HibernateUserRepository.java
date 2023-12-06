package ru.naumen.personalfinancebot.repositories.user;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.HibernateRepository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Optional;

/**
 * Реализация хранилища пользователей в БД с помощью библиотеки Hibernate
 */
public class HibernateUserRepository extends HibernateRepository implements UserRepository {
    public HibernateUserRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * Получает пользователя из БД по chat id из telegram.
     */
    @Override
    public Optional<User> getUserByTelegramChatId(Long chatId) {
        return produceTransaction(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);
            cq.select(root).where(cb.equal(root.get("chatId"), chatId));

            Query<User> query = session.createQuery(cq);
            return query.getResultStream().findFirst();
        });
    }

    /**
     * Сохраняет пользователя в БД
     */
    @Override
    public void saveUser(User user) {
        produceVoidTransaction(session -> {
            if (user.getId() == null) {
                session.persist(user);
            } else {
                session.merge(user);
            }
        });
    }

    /**
     * Удаляет пользователя с таким id из БД
     */
    @Override
    public void removeUserById(long id) {
        produceVoidTransaction(session -> {
            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
            }
        });
    }
}
