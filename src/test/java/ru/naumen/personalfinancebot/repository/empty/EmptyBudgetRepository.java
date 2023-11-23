package ru.naumen.personalfinancebot.repository.empty;

import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.budget.BudgetRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Хранилище с бюджетами, которое при любой операции бросит ${@link RuntimeException}
 */
public class EmptyBudgetRepository implements BudgetRepository {
    @Override
    public void saveBudget(Budget budget) {
        throw new RuntimeException("Budget repository shouldn't be touched");
    }

    @Override
    public Optional<Budget> getBudget(User user, YearMonth yearMonth) {
        throw new RuntimeException("Budget repository shouldn't be touched");
    }

    @Override
    public List<Budget> selectBudgetRange(User user, YearMonth from, YearMonth to) {
        throw new RuntimeException("Budget repository shouldn't be touched");
    }
}
