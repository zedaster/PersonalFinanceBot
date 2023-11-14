package ru.naumen.personalfinancebot.repositories.operation;

import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;

import java.util.List;

/**
 * Интерфейс репозитория модели данных "операция"
 */
public interface OperationRepository {
    Operation addOperation(User user, Category category, double payment);

    List<Operation> getFilteredByDate(User user, int month, int year);
}
