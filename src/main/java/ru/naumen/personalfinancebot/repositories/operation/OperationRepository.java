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
    /**
     * Добавляет запись операции в базу данных и возвращаёт её
     * @param user Пользователь
     * @param category Категория
     * @param payment Плата
     * @return Добавленная операция
     */
    Operation addOperation(User user, Category category, double payment);

    /**
     * Возвращает MAP, где ключ - название каатегории, значение - сумма операций по данной категории
     * @param user Пользователь
     * @param month Месяц
     * @param year Год
     * @param type Тип операции
     * @return Словарь с операциями
     */
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
