package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.repositories.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;

import java.time.YearMonth;
import java.util.Optional;

/**
 * Класс для обработки команды "/budget"
 */
public class SingleBudgetHandler implements CommandHandler {
    /**
     * Репозиторий для работы с бюджетом
     */
    private final BudgetRepository budgetRepository;

    /**
     * Репозиторий для работы с операциями
     */
    private final OperationRepository operationRepository;

    /**
     * @param budgetRepository    Репозиторий для работы с бюджетом
     * @param operationRepository Репозиторий для работы с операциями
     */
    public SingleBudgetHandler(BudgetRepository budgetRepository, OperationRepository operationRepository) {
        this.budgetRepository = budgetRepository;
        this.operationRepository = operationRepository;
    }

    /**
     * Метод, вызываемый при получении команды
     *
     * @param commandEvent
     */
    @Override
    public void handleCommand(HandleCommandEvent commandEvent) {
        YearMonth now = YearMonth.now();
        Optional<Budget> budget = this.budgetRepository.getBudget(commandEvent.getUser(), YearMonth.now());
        if (budget.isEmpty()) {
            // TODO
            return;
        }

        double incomePayment = this.operationRepository.getCurrentUserPaymentSummary(
                commandEvent.getUser(), CategoryType.INCOME, now
        );
        double expensePayment = this.operationRepository.getCurrentUserPaymentSummary(
                commandEvent.getUser(), CategoryType.EXPENSE, now
        );
        double currentUserBalance = commandEvent.getUser().getBalance();

        commandEvent.getBot().sendMessage(
                commandEvent.getUser(),
                "Бюджент на " + budget.get().getYearMonth()
                        + "Ожидаемые доходы: " + budget.get().getExpectedSummary(CategoryType.INCOME)
                        + "Ожидаемые расходы: " + budget.get().getExpectedSummary(CategoryType.EXPENSE)
                        + "Текущие доходы: " + incomePayment
                        + "Текущие расходы: " + expensePayment
                        + "Текущий баланс: " + currentUserBalance
                        + "Нужно ещё заработать: " + (budget.get().getExpectedSummary(CategoryType.INCOME) - incomePayment)
                        + "Ещё осталось на траты: " + (budget.get().getExpectedSummary(CategoryType.EXPENSE) - expensePayment)
        );
    }
}
