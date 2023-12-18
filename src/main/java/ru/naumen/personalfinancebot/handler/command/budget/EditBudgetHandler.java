package ru.naumen.personalfinancebot.handler.command.budget;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.repository.budget.BudgetRepository;
import ru.naumen.personalfinancebot.service.ArgumentParseService;
import ru.naumen.personalfinancebot.service.OutputFormatService;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Обработчик для команд "/budget_set_{"expenses" / "income"}"
 */
public class EditBudgetHandler implements CommandHandler {
    /**
     * Репозиторий для работы с бюджетом
     */
    private final BudgetRepository budgetRepository;

    /**
     * Сервис, который парсит аргументы
     */
    private final ArgumentParseService argumentParser;

    /**
     * Сервис, который приводит данные для вывода к нужному формату
     */
    private final OutputFormatService outputFormatter;

    /**
     * Тип, значение которого нужно изменить в записи бюджета
     */
    private final CategoryType type;

    /**
     * @param budgetRepository Репозиторий для работы с бюджетом
     * @param argumentParser   Сервис, который парсит аргументы
     * @param outputFormatter  Сервис, который приводит данные для вывода к нужному формату
     * @param type             Тип, значение которого нужно изменить в записи бюджета
     */
    public EditBudgetHandler(
            BudgetRepository budgetRepository, ArgumentParseService argumentParser, OutputFormatService outputFormatter,
            CategoryType type) {
        this.budgetRepository = budgetRepository;
        this.argumentParser = argumentParser;
        this.outputFormatter = outputFormatter;
        this.type = type;
    }

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        int argsCount = commandData.getArgs().size();
        YearMonth yearMonth = YearMonth.now();
        double amount;
        try {
            if (argsCount == 1) {
                amount = this.argumentParser.parsePositiveDouble(commandData.getArgs().get(0));
            } else if (argsCount == 2) {
                yearMonth = this.argumentParser.parseYearMonth(commandData.getArgs().get(0));
                amount = this.argumentParser.parsePositiveDouble(commandData.getArgs().get(1));
            } else {
                commandData.getBot().sendMessage(commandData.getUser(),
                        Message.INCORRECT_EDIT_BUDGET_ENTIRE_ARGS);
                return;
            }
        } catch (NumberFormatException e) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_BUDGET_NUMBER_ARG);
            return;
        } catch (DateTimeParseException e) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_BUDGET_YEAR_MONTH);
            return;
        }


        if (yearMonth.isBefore(YearMonth.now())) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.CANT_EDIT_OLD_BUDGET);
            return;
        }

        Optional<Budget> budget = this.budgetRepository.getBudget(session, commandData.getUser(), yearMonth);
        if (budget.isEmpty()) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.BUDGET_NOT_FOUND);
            return;
        }
        budget.get().setExpectedSummary(this.type, amount);
        this.budgetRepository.saveBudget(session, budget.get());

        double expectIncome = budget.get().getExpectedSummary(CategoryType.INCOME);
        double expectExpenses = budget.get().getExpectedSummary(CategoryType.EXPENSE);
        commandData.getBot().sendMessage(
                commandData.getUser(),
                Message.BUDGET_EDITED.formatted(
                        outputFormatter.formatRuMonthName(yearMonth.getMonth()),
                        String.valueOf(yearMonth.getYear()),
                        outputFormatter.formatDouble(expectIncome),
                        outputFormatter.formatDouble(expectExpenses)
                )
        );
    }
}
