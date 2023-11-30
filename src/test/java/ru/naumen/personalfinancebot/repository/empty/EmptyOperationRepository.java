package ru.naumen.personalfinancebot.repository.empty;

import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;

import java.time.YearMonth;
import java.util.Map;

/**
 * Хранилище с операциями, которое при любой операции бросит ${@link RuntimeException}
 */
public class EmptyOperationRepository implements OperationRepository {

    @Override
    public Operation addOperation(User user, Category category, double payment) {
        throw new RuntimeException("Operation repository shouldn't be touched");
    }

    @Override
    public Map<String, Double> getOperationsSumByType(User user, int month, int year, CategoryType type) {
        throw new RuntimeException("Operation repository shouldn't be touched");
    }

    @Override
    public double getCurrentUserPaymentSummary(User user, CategoryType type, YearMonth yearMonth) {
        throw new RuntimeException("Operation repository shouldn't be touched");
    }

    /**
     * Метод возвращает словарь, где ключ - название стандартной категории, значение - сумма операций по этой категории
     *
     * @param yearMonth Год-Месяц
     * @return Словарь<Категория, Плата>
     */
    @Override
    public Map<String, Double> getSummaryByStandardCategory(YearMonth yearMonth) {
        throw new RuntimeException("Operation repository shouldn't be touched");
    }
}
