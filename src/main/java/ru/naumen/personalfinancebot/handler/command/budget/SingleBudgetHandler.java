package ru.naumen.personalfinancebot.handler.command.budget;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.repository.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.service.OutputFormatService;

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
    public void handleCommand(CommandData commandData, Session session) {
        YearMonth currentMonthYear = YearMonth.now();
        Optional<Budget> budget = this.budgetRepository.getBudget(session, commandData.getUser(), YearMonth.now());
        if (budget.isEmpty()) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.CURRENT_BUDGET_NOT_EXISTS
                    .replace("{month}", outputFormatter.formatRuMonthName(currentMonthYear.getMonth()))
                    .replace("{year}", String.valueOf(currentMonthYear.getYear()))
            );
            return;
        }

        double expectIncome = budget.get().getExpectedSummary(CategoryType.INCOME);
        double expectExpenses = budget.get().getExpectedSummary(CategoryType.EXPENSE);
        double realIncome = this.operationRepository.getCurrentUserPaymentSummary(
                session,
                commandData.getUser(), CategoryType.INCOME, currentMonthYear
        );
        double realExpenses = this.operationRepository.getCurrentUserPaymentSummary(
                session,
                commandData.getUser(), CategoryType.EXPENSE, currentMonthYear
        );
        double incomeLeft = Math.max(0, expectIncome - realIncome);
        double expensesLeft = Math.max(0, expectExpenses - realExpenses);
        double balance = commandData.getUser().getBalance();

        commandData.getBot().sendMessage(commandData.getUser(), Message.CURRENT_BUDGET
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
