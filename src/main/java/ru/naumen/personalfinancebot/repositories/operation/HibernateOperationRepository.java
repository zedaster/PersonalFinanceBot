package ru.naumen.personalfinancebot.repositories.operation;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
     *
     * @param user     Пользователь, совершивший операцию
     * @param category Категория дохода/расхода
     * @param payment  Сумма
     * @return совершённая операция
     */
    @Override
    public Operation addOperation(User user, Category category, double payment) {
        try (Session session = this.sessionFactory.openSession()) {
            Operation operation = new Operation(user, category, payment);
            session.beginTransaction();
            session.save(operation);
            session.getTransaction().commit();
            return operation;
        }
    }

    /**
     * Возвращает словарь с названием категории и суммой расходов этой категории за указанный год и месяц
     *
     * @param user  Пользователь
     * @param month Месяц
     * @param year  Год
     * @return Список операций
     */
    @Override
    public Map<String, Double> getOperationsSumByType(User user, int month, int year, CategoryType type) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);
        try (Session session = this.sessionFactory.openSession()) {
            String hql = "SELECT cat.categoryName, sum (operation.payment) " +
                    "FROM Operation operation " +
                    "LEFT JOIN operation.category cat on cat.id = operation.category.id " +
                    "WHERE cat.type = :categoryType " +
                    "AND (operation.user = :user OR operation.user = NULL) " +
                    "AND operation.createdAt BETWEEN :startDate AND :endDate " +
                    "GROUP BY operation.category.id, cat.id";

            List<?> operations = session.createQuery(hql)
                    .setParameter("categoryType", type)
                    .setParameter("user", user)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
            if (operations.isEmpty()) {
                return null;
            }
            Map<String, Double> result = new LinkedHashMap<>();
            for (Object operation : operations) {
                Object[] row = (Object[]) operation;
                String category = (String) row[0];
                Double payment = (Double) row[1];
                result.put(category, payment);
            }
            return result;
        }
    }

    /**
     * Метод возвращает сумму операций пользователя указанного типа (расход/доход) за определённый месяц
     *
     * @param user      Пользователь
     * @param type      Тип операции
     * @param yearMonth Месяц, год
     * @return Сумма операций
     */
    @Override
    public double getCurrentUserPaymentSummary(User user, CategoryType type, YearMonth yearMonth) {
        LocalDate startDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);
        LocalDate endDate = startDate.plusMonths(1);
        try (Session session = this.sessionFactory.openSession()) {
            String hql = "SELECT sum(Operation.payment) from Operation "
                    + "LEFT JOIN Category on Category.id = Operation.category.id "
                    + "WHERE Operation.user = :user "
                    + "AND Operation.createdAt BETWEEN :startDate AND :endDate "
                    + "AND Category.type = :type";

            double paymentSummary = (double) session
                    .createQuery(hql)
                    .setParameter("user", user)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .setParameter("type", type)
                    .uniqueResult();
            return paymentSummary;
        }
    }
}
