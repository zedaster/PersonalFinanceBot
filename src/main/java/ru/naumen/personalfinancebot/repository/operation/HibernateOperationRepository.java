package ru.naumen.personalfinancebot.repository.operation;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Репозиторий модели данных "Операция" с использованием Hibernate
 */
public class HibernateOperationRepository implements OperationRepository {

    @Override
    public Operation addOperation(Session session, User user, Category category, double payment) {
        Operation operation = new Operation(user, category, payment);
        session.save(operation);
        return operation;
    }

    @Override
    public Map<String, Double> getOperationsSumByType(Session session, User user, int month, int year, CategoryType type) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);

        final String hql = "SELECT cat.categoryName, sum (operation.payment) " +
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

    @Override
    public double getCurrentUserPaymentSummary(Session session, User user, CategoryType type, YearMonth yearMonth) {
        LocalDate startDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        String hql = "SELECT sum(op.payment) from Operation op "
                + "LEFT JOIN Category cat on cat.id = op.category.id "
                + "WHERE op.user = :user "
                + "AND cat.type = :type "
                + "AND op.createdAt BETWEEN :startDate AND :endDate";

        Object paymentSummary = session
                .createQuery(hql)
                .setParameter("user", user)
                .setParameter("type", type)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .uniqueResult();
        if (paymentSummary == null) {
            return 0.0;
        }
        return (double) paymentSummary;
    }
}
