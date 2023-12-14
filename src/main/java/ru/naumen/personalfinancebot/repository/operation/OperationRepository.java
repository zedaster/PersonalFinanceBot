package ru.naumen.personalfinancebot.repository.operation;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;

import java.time.YearMonth;
import java.util.Map;

/**
 * Интерфейс репозитория модели данных "операция"
 */
public interface OperationRepository {
    /**
     * Добавляет запись операции в базу данных и возвращаёт её
     *
     * @param user     Пользователь
     * @param category Категория
     * @param payment  Плата
     * @return Добавленная операция
     */
    Operation addOperation(Session session, User user, Category category, double payment);

    /**
     * Возвращает MAP, где ключ - название каатегории, значение - сумма операций по данной категории
     *
     * @param user  Пользователь
     * @param month Месяц
     * @param year  Год
     * @param type  Тип операции
     * @return Словарь с операциями
     */
    Map<String, Double> getOperationsSumByType(Session session, User user, int month, int year, CategoryType type);

    /**
     * Метод возвращает сумму операций пользователя указанного типа (расход/доход) за определённый месяц
     * @param user Пользователь
     * @param type Тип операции
     * @param yearMonth Месяц, год
     * @return Сумма операций
     */
    double getCurrentUserPaymentSummary(Session session, User user, CategoryType type, YearMonth yearMonth);

    /**
     * Возвращает средние месячные общие доходы и расходы по всем пользователям
     *
     * @param yearMonth Год-месяц, за который выдаются данные
     * @return Словарь<Тип, Сумма> или null, если нет данных
     */
    Map<CategoryType, Double> getEstimateSummary(Session session, YearMonth yearMonth);
}
