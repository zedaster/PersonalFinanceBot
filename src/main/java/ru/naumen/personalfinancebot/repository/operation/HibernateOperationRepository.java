package ru.naumen.personalfinancebot.repository.operation;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;

import java.time.LocalDateTime;
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
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);

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
}
