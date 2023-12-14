package ru.naumen.personalfinancebot.repository.operation;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
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

    /**
     * Метод возвращает сумму операций пользователя указанного типа (расход/доход) за определённый месяц
     *
     * @param user      Пользователь
     * @param type      Тип операции
     * @param yearMonth Месяц, год
     * @return Сумма операций
     */
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

    @Override
    public Map<CategoryType, Double> getEstimateSummary(Session session, YearMonth yearMonth) {
        // HQL не поддерживает вложенный запрос в FROM

//            String hql = "SELECT paymentSums.type, avg(paymentSums.payments) FROM "
//                    + "(SELECT categories.type AS type, sum(operations.payment) AS payments "
//                    + "FROM Operation operations "
//                    + "JOIN operations.category categories "
//                    + "WHERE year(operations.createdAt) = :year "
//                    + "AND month(operations.createdAt) = :month "
//                    + "GROUP BY operations.user, categories.type) AS paymentSums "
//                    + "GROUP BY paymentSums.type";

        // Поэтому берем суммы и считаем среднее в Java

        String hql = "SELECT categories.type, sum(operations.payment) AS payments "
                     + "FROM Operation operations "
                     + "JOIN operations.category categories "
                     + "WHERE year(operations.createdAt) = :year "
                     + "AND month(operations.createdAt) = :month "
                     + "GROUP BY operations.user, categories.type";

        List<?> sumRows = session.createQuery(hql)
                .setParameter("year", yearMonth.getYear())
                .setParameter("month", yearMonth.getMonth().getValue())
                .getResultList();

        if (sumRows.isEmpty()) {
            return null;
        }

        return calculateAverageForEachCategory(sumRows);
    }

    /**
     * Высчитывает среднюю величину значений для каждой категории
     *
     * @param sumRows Список строк из БД, полученый из hibernate. В нем должны быть CategoryType, затем сумма
     * @return Словарь<Тип категории, Среднее>
     */
    private Map<CategoryType, Double> calculateAverageForEachCategory(List<?> sumRows) {
        Map<CategoryType, Double> result = new HashMap<>();
        Map<CategoryType, Integer> count = new HashMap<>();

        for (Object rawRow : sumRows) {
            Object[] row = (Object[]) rawRow;
            CategoryType type = (CategoryType) row[0];
            double sum = (double) row[1];

            result.put(type, result.getOrDefault(type, 0.0) + sum);
            count.put(type, count.getOrDefault(type, 0) + 1);
        }

        result.replaceAll((type, value) -> result.get(type) / count.get(type));
        return result;
    }
}
