package ru.naumen.personalfinancebot.repositories.operation;

import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс репозитория модели данных "операция"
 */
public interface OperationRepository {
    Operation addOperation(User user, Category category, double payment);

    Map<String, Double> getOperationsSumByType(User user, int month, int year, CategoryType type);
}
