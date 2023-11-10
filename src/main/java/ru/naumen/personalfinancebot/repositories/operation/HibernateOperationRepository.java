package ru.naumen.personalfinancebot.repositories.operation;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;

/**
 * Репозиторий модели данных "Операция"
 */
public class HibernateOperationRepository implements OperationRepository {
    private final SessionFactory sessionFactory;

    public HibernateOperationRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Класс для добавления операции
     * @param user Пользователь, совершивший операцию
     * @param category Категория дохода/расхода
     * @param payment Сумма
     * @return совершённая операция
     */
    @Override
    public Operation addOperation(User user, Category category, double payment) {
        try (Session session = this.sessionFactory.openSession()) {
            Operation operation = new Operation(user, category, payment);
            session.beginTransaction();
            session.save(operation);
            session.getTransaction().commit();
            session.close();
            return operation;
        }
    }
}
