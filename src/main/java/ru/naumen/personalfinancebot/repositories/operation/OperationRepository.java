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
     * Метод для добавления операции
     *
     * @param user     Пользователь, совершивший операцию
     * @param category Категория дохода/расхода
     * @param payment  Сумма
     * @return совершённая операция
     */
    Operation addOperation(User user, Category category, double payment);

    /**
     * Возвращает словарь с названием категории и суммой расходов/доходов этой категории за указанный год и месяц
     *
     * @param user  Пользователь
     * @param month Месяц
     * @param year  Год
     * @return Список операций или null, если запрашиваемые операции отсутствуют
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

    /**
     * Метод возвращает словарь, где ключ - название стандартной категории, значение - сумма операций по этой категории
     * @param yearMonth Год-Месяц
     * @return Словарь<Категория, Плата>
     */
    Map<String, Double> getAverageSummaryByStandardCategory(YearMonth yearMonth);
}
