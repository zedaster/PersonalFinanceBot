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
import ru.naumen.personalfinancebot.service.DateParseService;
import ru.naumen.personalfinancebot.service.NumberParseService;
import ru.naumen.personalfinancebot.service.OutputMonthFormatService;
import ru.naumen.personalfinancebot.service.OutputNumberFormatService;

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
     * Сервис, который парсит дату
     */
    private final DateParseService dateParseService;

    /**
     * Сервис, который парсит числа
     */
    private final NumberParseService numberParseService;

    /**
     * Сервис, который форматирует числа
     */
    private final OutputNumberFormatService numberFormatService;

    /**
     * Сервис, который форматирует месяц к русскому названию
     */
    private final OutputMonthFormatService monthFormatService;


    /**
     * @param budgetRepository    Репозиторий для работы с бюджетом
     * @param operationRepository Репозиторий для работы с операциями
     * @param dateParseService    Сервис, который парсит дату
     * @param numberParseService  Сервис, который парсит числа
     * @param numberFormatService Сервис, который форматирует числа
     * @param monthFormatService  Сервис, который форматирует месяц к русскому названию
     */
    public CreateBudgetHandler(BudgetRepository budgetRepository, OperationRepository operationRepository,
                               DateParseService dateParseService, NumberParseService numberParseService,
                               OutputNumberFormatService numberFormatService, OutputMonthFormatService monthFormatService) {
        this.budgetRepository = budgetRepository;
        this.operationRepository = operationRepository;
        this.dateParseService = dateParseService;
        this.numberParseService = numberParseService;
        this.numberFormatService = numberFormatService;
        this.monthFormatService = monthFormatService;
    }

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        if (commandData.getArgs().size() != 3) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_CREATE_BUDGET_ENTIRE_ARGS);
            return;
        }

        YearMonth yearMonth;
        try {
            yearMonth = this.dateParseService.parseYearMonth(commandData.getArgs().get(0));
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
            expectedIncome = this.numberParseService.parsePositiveDouble(commandData.getArgs().get(1));
            expectedExpenses = this.numberParseService.parsePositiveDouble(commandData.getArgs().get(2));
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
        budget.setIncome(expectedIncome);
        budget.setExpense(expectedExpenses);
        budget.setTargetDate(yearMonth);
        budget.setUser(user);
        budgetRepository.saveBudget(session, budget);

        commandData.getBot().sendMessage(
                user,
                Message.BUDGET_CREATED.formatted(
                        monthFormatService.formatRuMonthName(yearMonth.getMonth()),
                        String.valueOf(yearMonth.getYear()),
                        numberFormatService.formatDouble(expectedIncome),
                        numberFormatService.formatDouble(expectedExpenses),
                        numberFormatService.formatDouble(currentIncome),
                        numberFormatService.formatDouble(currentExpenses),
                        numberFormatService.formatDouble(balance),
                        numberFormatService.formatDouble(incomeLeft),
                        numberFormatService.formatDouble(expensesLeft)
                )
        );
    }
}
