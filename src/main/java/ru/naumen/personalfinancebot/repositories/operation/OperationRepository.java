package ru.naumen.personalfinancebot.repositories.operation;

import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;

/**
 * Интерфейс репозитория модели данных "операция"
 */
public interface OperationRepository {
    Operation addOperation(User user, Category category, double payment);
}
