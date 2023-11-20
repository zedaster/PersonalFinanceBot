package ru.naumen.personalfinancebot.repositories.budget;

import com.sun.istack.Nullable;
import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.User;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public class HibernateBudgetRepository implements BudgetRepository{
    protected final SessionFactory sessionFactory;

    public HibernateBudgetRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Сохраняет бюджет в БД
     *
     * @param budget Бюджет
     */
    @Override
    public void saveBudget(Budget budget) {

    }

    /**
     * Возвращает бюджет пользователя за Месяц-Год
     *
     * @param user      Пользователь
     * @param yearMonth Месяц-Год
     * @return Бюджет пользователя
     */
    @Override
    public Optional<Budget> getBudget(User user, @Nullable YearMonth yearMonth) {
        return Optional.empty();
    }

    /**
     * Возвращает список бюджетов для пользователя, за заданный промежуток
     *
     * @param user Пользователь
     * @param from Месяц-Год начала диапазона
     * @param to   Месяц-Год конца диапазона
     * @return Список бюджетов
     */
    @Override
    public List<Budget> selectBudgetRange(User user, YearMonth from, YearMonth to) {
        return null;
    }
}
