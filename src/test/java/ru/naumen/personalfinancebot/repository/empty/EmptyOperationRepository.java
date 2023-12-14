package ru.naumen.personalfinancebot.repository.empty;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;

import java.time.YearMonth;
import java.util.Map;

/**
 * Хранилище с операциями, которое при любой операции бросит ${@link RuntimeException}
 */
public class EmptyOperationRepository implements OperationRepository {

    @Override
    public Operation addOperation(Session session, User user, Category category, double payment) {
        throw new RuntimeException("Operation repository shouldn't be touched");
    }

    @Override
    public Map<String, Double> getOperationsSumByType(Session session, User user, int month, int year, CategoryType type) {
        throw new RuntimeException("Operation repository shouldn't be touched");
    }

    @Override
    public double getCurrentUserPaymentSummary(Session session, User user, CategoryType type, YearMonth yearMonth) {
        throw new RuntimeException("Operation repository shouldn't be touched");
    }

    @Override
    public Map<CategoryType, Double> getEstimateSummary(Session session, YearMonth yearMonth) {
        throw new RuntimeException("Operation repository shouldn't be touched");
    }
}
