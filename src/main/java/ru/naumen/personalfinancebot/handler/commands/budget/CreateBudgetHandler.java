package ru.naumen.personalfinancebot.handler.commands.budget;

import ru.naumen.personalfinancebot.handler.commands.CommandHandler;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.services.ArgumentParseService;
import ru.naumen.personalfinancebot.services.OutputFormatService;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Обработчик команды "/budget_create".
 */
public class CreateBudgetHandler implements CommandHandler {
    /**
     * Репозиторий для работы с бюджетом
     */
    private final BudgetRepository budgetRepository;

    /**
     * Репозиторий для работы с операциями
     */
    private final OperationRepository operationRepository;

    /**
     * Сервис, который парсит аргументы
     */
    private final ArgumentParseService argumentParseService;

    /**
     * Сервис, который приводит данные для вывода к нужному формату
     */
    private final OutputFormatService outputFormatter;

    /**
     * @param budgetRepository     Репозиторий для работы с бюджетом
     * @param operationRepository  Репозиторий для работы с операциями
     * @param argumentParseService Сервис, который парсит аргументы
     * @param outputFormatter      Сервис, который приводит данные для вывода к нужному формату
     */
    public CreateBudgetHandler(BudgetRepository budgetRepository, OperationRepository operationRepository,
                               ArgumentParseService argumentParseService, OutputFormatService outputFormatter) {
        this.budgetRepository = budgetRepository;
        this.operationRepository = operationRepository;
        this.argumentParseService = argumentParseService;
        this.outputFormatter = outputFormatter;
    }

    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(HandleCommandEvent commandEvent) {
        if (commandEvent.getArgs().size() != 3) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.INCORRECT_CREATE_BUDGET_ENTIRE_ARGS);
            return;
        }

        YearMonth yearMonth;
        try {
            yearMonth = this.argumentParseService.parseYearMonth(commandEvent.getArgs().get(0));
        } catch (DateTimeParseException e) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.INCORRECT_BUDGET_YEAR_MONTH);
            return;
        }

        if (yearMonth.isBefore(YearMonth.now())) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.CANT_CREATE_OLD_BUDGET);
            return;
        }

        double expectedIncome;
        double expectedExpenses;
        try {
            expectedIncome = this.argumentParseService.parsePositiveDouble(commandEvent.getArgs().get(1));
            expectedExpenses = this.argumentParseService.parsePositiveDouble(commandEvent.getArgs().get(2));
        } catch (NumberFormatException e) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.INCORRECT_BUDGET_NUMBER_ARG);
            return;
        }

        User user = commandEvent.getUser();
        double balance = user.getBalance();
        double currentIncome = this.operationRepository.getCurrentUserPaymentSummary(user, CategoryType.INCOME, yearMonth);
        double currentExpenses = this.operationRepository.getCurrentUserPaymentSummary(user, CategoryType.EXPENSE, yearMonth);
        double incomeLeft = expectedIncome - currentIncome;
        double expensesLeft = expectedExpenses - currentExpenses;

        Budget budget = new Budget();
        budget.setExpectedSummary(CategoryType.INCOME, expectedIncome);
        budget.setExpectedSummary(CategoryType.EXPENSE, expectedExpenses);
        budget.setTargetDate(yearMonth);
        budget.setUser(user);
        budgetRepository.saveBudget(budget);

        commandEvent.getBot().sendMessage(user, StaticMessages.BUDGET_CREATED
                .replace("{month}", outputFormatter.formatRuMonthName(yearMonth.getMonth()))
                .replace("{year}", String.valueOf(yearMonth.getYear()))
                .replace("{expect_income}", outputFormatter.formatDouble(expectedIncome))
                .replace("{expect_expenses}", outputFormatter.formatDouble(expectedExpenses))
                .replace("{current_income}", outputFormatter.formatDouble(currentIncome))
                .replace("{current_expenses}", outputFormatter.formatDouble(currentExpenses))
                .replace("{balance}", outputFormatter.formatDouble(balance))
                .replace("{income_left}", outputFormatter.formatDouble(incomeLeft))
                .replace("{expenses_left}", outputFormatter.formatDouble(expensesLeft))
        );
    }
}
