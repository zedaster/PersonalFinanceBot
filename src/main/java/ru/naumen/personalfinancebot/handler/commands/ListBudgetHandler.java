package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.repositories.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;

import java.time.YearMonth;
import java.util.List;

/**
 * Обработчик для команды "/budget_list"
 */
public class ListBudgetHandler implements CommandHandler {
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
    public ListBudgetHandler(BudgetRepository budgetRepository, OperationRepository operationRepository) {
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
        List<String> arguments = commandEvent.getArgs();
        YearMonth from = YearMonth.now(), to = YearMonth.now();
        if (arguments.isEmpty()) {
            // TODO Список аргументов пуст -> необходимо указать диапазон за текущий год
        } else if (arguments.size() == 1) {
            // TODO В списке аргументов указан год, по которому надо укзаать диапазон
        } else if (arguments.size() == 2) {
            // TODO пользователь явно указал диапазон
        } else {
            //TODO ERROR
            return;
        }
        // Список бюджетов за указанный период;
        List<Budget> budgets = this.budgetRepository.selectBudgetRange(commandEvent.getUser(), from, to);
        StringBuilder resultReplyMessage = new StringBuilder();
        resultReplyMessage.append("Ваши запланированные доходы и расходы по месяцам");
        for (Budget budget : budgets) {
            double incomeOperationsSummary = this.operationRepository
                    .getCurrentUserPaymentSummary(commandEvent.getUser(), CategoryType.INCOME, budget.getYearMonth());
            double expenseOperationSummary = this.operationRepository
                    .getCurrentUserPaymentSummary(commandEvent.getUser(), CategoryType.EXPENSE, budget.getYearMonth());
            resultReplyMessage
                    .append(budget.getYearMonth().toString())
                    .append("Ожидание: +").append(budget.getExpectedSummary(CategoryType.INCOME)).append(" | -")
                    .append(budget.getExpectedSummary(CategoryType.EXPENSE))
                    .append("\nРеальность: +").append(incomeOperationsSummary).append(" | -")
                    .append(expenseOperationSummary)
                    .append("\n");
        }
        commandEvent.getBot().sendMessage(commandEvent.getUser(), resultReplyMessage.toString());
    }
}
