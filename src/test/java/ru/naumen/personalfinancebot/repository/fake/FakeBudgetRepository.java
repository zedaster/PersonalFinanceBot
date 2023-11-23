package ru.naumen.personalfinancebot.repository.fake;

import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repository.budget.BudgetRepository;

import java.time.YearMonth;
import java.util.*;

/**
 * Хранилище бюджетов для тестов
 */
public class FakeBudgetRepository implements BudgetRepository {
    /**
     * Коллекция, в которой хранятся все бюджеты
     */
    private final Map<UserYearMonth, Budget> budgets = new HashMap<>();

    /**
     * Сохраняет бюджет
     *
     * @param budget Бюджет
     */
    @Override
    public void saveBudget(Budget budget) {
        budgets.put(new UserYearMonth(budget.getUser(), budget.getTargetDate()), budget);
    }

    /**
     * Возвращает бюджет пользователя за Месяц-Год
     *
     * @param user      Пользователь
     * @param yearMonth Месяц-Год
     * @return Бюджет пользователя
     */
    @Override
    public Optional<Budget> getBudget(User user, YearMonth yearMonth) {
        return Optional.ofNullable(budgets.get(new UserYearMonth(user, yearMonth)));
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
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From must be before to");
        }
        List<Budget> result = new ArrayList<>();
        YearMonth ym = from;
        while (!ym.isAfter(to)) {
            Budget budget = budgets.get(new UserYearMonth(user, ym));
            if (budget != null) {
                result.add(budget);
            }
            ym = ym.plusMonths(1);
        }
        return result;
    }

    /**
     * Запись, содержащая в себе пользователя и месяц-год. Необходима для разделения бюджетов в коллекции.
     *
     * @param user
     * @param yearMonth
     */
    private record UserYearMonth(User user, YearMonth yearMonth) {

    }
}
