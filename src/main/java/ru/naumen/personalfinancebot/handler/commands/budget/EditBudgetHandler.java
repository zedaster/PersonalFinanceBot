package ru.naumen.personalfinancebot.handler.commands.budget;

import ru.naumen.personalfinancebot.handler.commands.CommandHandler;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.repositories.budget.BudgetRepository;
import ru.naumen.personalfinancebot.services.ArgumentParseService;
import ru.naumen.personalfinancebot.services.OutputFormatService;

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

    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(HandleCommandEvent commandEvent) {
        int argsCount = commandEvent.getArgs().size();
        YearMonth yearMonth = YearMonth.now();
        double amount;
        try {
            if (argsCount == 1) {
                amount = this.argumentParser.parsePositiveDouble(commandEvent.getArgs().get(0));
            } else if (argsCount == 2) {
                yearMonth = this.argumentParser.parseYearMonth(commandEvent.getArgs().get(0));
                amount = this.argumentParser.parsePositiveDouble(commandEvent.getArgs().get(1));
            } else {
                commandEvent.getBot().sendMessage(commandEvent.getUser(),
                        StaticMessages.INCORRECT_EDIT_BUDGET_ENTIRE_ARGS);
                return;
            }
        } catch (NumberFormatException e) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.INCORRECT_BUDGET_NUMBER_ARG);
            return;
        } catch (DateTimeParseException e) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.INCORRECT_BUDGET_YEAR_MONTH);
            return;
        }


        if (yearMonth.isBefore(YearMonth.now())) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.CANT_EDIT_OLD_BUDGET);
            return;
        }

        Optional<Budget> budget = this.budgetRepository.getBudget(commandEvent.getUser(), yearMonth);
        if (budget.isEmpty()) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.BUDGET_NOT_FOUND);
            return;
        }
        budget.get().setExpectedSummary(this.type, amount);
        this.budgetRepository.saveBudget(budget.get());

        double expectIncome = budget.get().getExpectedSummary(CategoryType.INCOME);
        double expectExpenses = budget.get().getExpectedSummary(CategoryType.EXPENSE);
        commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.BUDGET_EDITED
                .replace("{month}", outputFormatter.formatRuMonthName(yearMonth.getMonth()))
                .replace("{year}", String.valueOf(yearMonth.getYear()))
                .replace("{expect_income}", outputFormatter.formatDouble(expectIncome))
                .replace("{expect_expenses}", outputFormatter.formatDouble(expectExpenses))
        );
    }
}
