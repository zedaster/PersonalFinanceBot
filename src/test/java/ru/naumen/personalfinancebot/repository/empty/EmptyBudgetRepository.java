package ru.naumen.personalfinancebot.repository.empty;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.budget.BudgetRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Хранилище с бюджетами, которое при любой операции бросит ${@link RuntimeException}
 */
public class EmptyBudgetRepository implements BudgetRepository {
    @Override
    public void saveBudget(Session session, Budget budget) {
        throw new RuntimeException("Budget repository shouldn't be touched");
    }

    @Override
    public Optional<Budget> getBudget(Session session, User user, YearMonth yearMonth) {
        throw new RuntimeException("Budget repository shouldn't be touched");
    }

    @Override
    public List<Budget> selectBudgetRange(Session session, User user, YearMonth from, YearMonth to) {
        throw new RuntimeException("Budget repository shouldn't be touched");
    }
}
