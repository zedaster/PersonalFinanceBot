package ru.naumen.personalfinancebot.repositories.operation;

import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.HibernateRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Репозиторий модели данных "Операция"
 */
public class HibernateOperationRepository extends HibernateRepository implements OperationRepository {
    public HibernateOperationRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
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
        Operation operation = new Operation(user, category, payment);
        produceVoidTransaction(session -> session.save(operation));
        return operation;
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
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);

        final String hql = "SELECT cat.categoryName, sum (operation.payment) " +
                "FROM Operation operation " +
                "LEFT JOIN operation.category cat on cat.id = operation.category.id " +
                "WHERE cat.type = :categoryType " +
                "AND (operation.user = :user OR operation.user = NULL) " +
                "AND operation.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY operation.category.id, cat.id";

        return produceTransaction(session -> {
            List<?> operations = session.createQuery(hql)
                    .setParameter("categoryType", type)
                    .setParameter("user", user)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
            if (operations.isEmpty()){
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
        });
    }
}
