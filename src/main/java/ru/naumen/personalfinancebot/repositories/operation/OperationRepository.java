package ru.naumen.personalfinancebot.repositories.operation;

import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;

import java.time.YearMonth;
import java.util.Map;

/**
 * Интерфейс репозитория модели данных "операция"
 */
public interface OperationRepository {
    Operation addOperation(User user, Category category, double payment);

    Map<String, Double> getOperationsSumByType(User user, int month, int year, CategoryType type);

    /**
     * Метод возвращает сумму операций пользователя указанного типа (расход/доход) за определённый месяц
     * @param user Пользователь
     * @param type Тип операции
     * @param yearMonth Месяц, год
     * @return Сумма операций
     */
    double getCurrentUserPaymentSummary(User user, CategoryType type, YearMonth yearMonth);
}
