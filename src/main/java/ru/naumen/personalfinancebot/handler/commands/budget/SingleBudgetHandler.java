package ru.naumen.personalfinancebot.handler.commands.budget;

import ru.naumen.personalfinancebot.handler.commands.CommandHandler;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.repositories.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.services.OutputFormatService;

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
     * Сервис, который приводит данные для вывода к нужному формату
     */
    private final OutputFormatService outputFormatter;

    /**
     * @param budgetRepository    Репозиторий для работы с бюджетом
     * @param operationRepository Репозиторий для работы с операциями
     * @param outputFormatter     Сервис, который приводит данные для вывода к нужному формату
     */
    public SingleBudgetHandler(BudgetRepository budgetRepository, OperationRepository operationRepository, OutputFormatService outputFormatter) {
        this.budgetRepository = budgetRepository;
        this.operationRepository = operationRepository;
        this.outputFormatter = outputFormatter;
    }

    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(HandleCommandEvent commandEvent) {
        YearMonth currentMonthYear = YearMonth.now();
        Optional<Budget> budget = this.budgetRepository.getBudget(commandEvent.getUser(), YearMonth.now());
        if (budget.isEmpty()) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.CURRENT_BUDGET_NOT_EXISTS
                    .replace("{month}", outputFormatter.formatRuMonthName(currentMonthYear.getMonth()))
                    .replace("{year}", String.valueOf(currentMonthYear.getYear()))
            );
            return;
        }

        double expectIncome = budget.get().getExpectedSummary(CategoryType.INCOME);
        double expectExpenses = budget.get().getExpectedSummary(CategoryType.EXPENSE);
        double realIncome = this.operationRepository.getCurrentUserPaymentSummary(
                commandEvent.getUser(), CategoryType.INCOME, currentMonthYear
        );
        double realExpenses = this.operationRepository.getCurrentUserPaymentSummary(
                commandEvent.getUser(), CategoryType.EXPENSE, currentMonthYear
        );
        double incomeLeft = expectIncome - realIncome;
        double expensesLeft = expectExpenses - realExpenses;
        double balance = commandEvent.getUser().getBalance();

        commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.CURRENT_BUDGET
                .replace("{month}", outputFormatter.formatRuMonthName(currentMonthYear.getMonth()))
                .replace("{year}", String.valueOf(currentMonthYear.getYear()))
                .replace("{expect_income}", outputFormatter.formatDouble(expectIncome))
                .replace("{expect_expenses}", outputFormatter.formatDouble(expectExpenses))
                .replace("{real_income}", outputFormatter.formatDouble(realIncome))
                .replace("{real_expenses}", outputFormatter.formatDouble(realExpenses))
                .replace("{balance}", outputFormatter.formatDouble(balance))
                .replace("{income_left}", outputFormatter.formatDouble(incomeLeft))
                .replace("{expenses_left}", outputFormatter.formatDouble(expensesLeft))
        );
    }
}
