package ru.naumen.personalfinancebot.repository.hibernate;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;

import java.time.LocalDate;

public class TestHibernateOperationRepository extends HibernateOperationRepository {
    /**
     * Объект, позволяющий совершать транзакции для тестовых хранилищ
     */
    private final TestHibernateTransaction testHibernateTransactions;

    public TestHibernateOperationRepository() {
        testHibernateTransactions = new TestHibernateTransaction(Operation.class);
    }

    /**
     * Метод для добавления операции с определенной датой
     *
     * @param user      Пользователь, совершивший операцию
     * @param category  Категория дохода/расхода
     * @param payment   Сумма
     * @param createdAt Дата создания операции
     * @return совершённая операция
     */
    public Operation addOperation(Session session, User user, Category category, double payment, LocalDate createdAt) {
        Operation operation = new Operation(user, category, payment, createdAt);
        session.save(operation);
        return operation;
    }

    /**
     * Очистка всех данных в таблице с операциями
     */
    public void removeAll(Session session) {
        testHibernateTransactions.removeAll(session);
    }
}
