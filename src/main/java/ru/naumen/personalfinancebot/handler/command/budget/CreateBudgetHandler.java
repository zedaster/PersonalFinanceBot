package ru.naumen.personalfinancebot.handler.command.budget;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.service.ArgumentParseService;
import ru.naumen.personalfinancebot.service.OutputFormatService;

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

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        if (commandData.getArgs().size() != 3) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_CREATE_BUDGET_ENTIRE_ARGS);
            return;
        }

        YearMonth yearMonth;
        try {
            yearMonth = this.argumentParseService.parseYearMonth(commandData.getArgs().get(0));
        } catch (DateTimeParseException e) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_BUDGET_YEAR_MONTH);
            return;
        }

        if (yearMonth.isBefore(YearMonth.now())) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.CANT_CREATE_OLD_BUDGET);
            return;
        }

        double expectedIncome;
        double expectedExpenses;
        try {
            expectedIncome = this.argumentParseService.parsePositiveDouble(commandData.getArgs().get(1));
            expectedExpenses = this.argumentParseService.parsePositiveDouble(commandData.getArgs().get(2));
        } catch (NumberFormatException e) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_BUDGET_NUMBER_ARG);
            return;
        }

        User user = commandData.getUser();
        double balance = user.getBalance();
        double currentIncome = this.operationRepository.getCurrentUserPaymentSummary(session, user, CategoryType.INCOME, yearMonth);
        double currentExpenses = this.operationRepository.getCurrentUserPaymentSummary(session, user, CategoryType.EXPENSE, yearMonth);
        double incomeLeft = expectedIncome - currentIncome;
        double expensesLeft = expectedExpenses - currentExpenses;

        Budget budget = new Budget();
        budget.setExpectedSummary(CategoryType.INCOME, expectedIncome);
        budget.setExpectedSummary(CategoryType.EXPENSE, expectedExpenses);
        budget.setTargetDate(yearMonth);
        budget.setUser(user);
        budgetRepository.saveBudget(session, budget);

        commandData.getBot().sendMessage(
                user,
                Message.BUDGET_CREATED.formatted(
                        outputFormatter.formatRuMonthName(yearMonth.getMonth()),
                        String.valueOf(yearMonth.getYear()),
                        outputFormatter.formatDouble(expectedIncome),
                        outputFormatter.formatDouble(expectedExpenses),
                        outputFormatter.formatDouble(currentIncome),
                        outputFormatter.formatDouble(currentExpenses),
                        outputFormatter.formatDouble(balance),
                        outputFormatter.formatDouble(incomeLeft),
                        outputFormatter.formatDouble(expensesLeft)
                )
        );
    }
}
