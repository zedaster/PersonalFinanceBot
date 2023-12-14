package ru.naumen.personalfinancebot.repository.budget;

import com.sun.istack.Nullable;
import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.model.User;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Класс для работы бюджета с хранилищем;
 */
public interface BudgetRepository {
    /**
     * Сохраняет бюджет в БД
     *
     * @param budget Бюджет
     */
    void saveBudget(Session session, Budget budget);

    /**
     * Возвращает бюджет пользователя за Месяц-Год
     *
     * @param user      Пользователь
     * @param yearMonth Месяц-Год
     * @return Бюджет пользователя
     */
    Optional<Budget> getBudget(Session session, User user, @Nullable YearMonth yearMonth);

    /**
     * Возвращает список бюджетов для пользователя, за заданный промежуток
     *
     * @param user Пользователь
     * @param from Месяц-Год начала диапазона
     * @param to   Месяц-Год конца диапазона
     * @return Список бюджетов
     */
    List<Budget> selectBudgetRange(Session session, User user, YearMonth from, YearMonth to);
}
