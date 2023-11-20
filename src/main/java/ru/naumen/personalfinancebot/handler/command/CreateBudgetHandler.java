package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.repositories.budget.BudgetRepository;
import ru.naumen.personalfinancebot.services.ArgumentParseService;

import java.time.YearMonth;

/**
 * Обработчик команды "/budget_create".
 */
public class CreateBudgetHandler implements CommandHandler {
    /**
     * Репозиторий для работы с бюджетом
     */
    private final BudgetRepository budgetRepository;

    /**
     * Сервис, который парсит аргументы
     */
    private final ArgumentParseService argumentParseService;

    /**
     * @param budgetRepository     Репозиторий для работы с бюджетом
     * @param argumentParseService Сервис, который парсит аргументы
     */
    public CreateBudgetHandler(BudgetRepository budgetRepository, ArgumentParseService argumentParseService) {
        this.budgetRepository = budgetRepository;
        this.argumentParseService = argumentParseService;
    }

    /**
     * Метод, вызываемый при получении команды
     *
     * @param commandEvent
     */
    @Override
    public void handleCommand(HandleCommandEvent commandEvent) {
        // TODO ArgumentParseService
        if (commandEvent.getArgs().size() != 3) {
            // TODO Пользователь  ввёл неверное кол-во аргументов
            return;
        }
        // TODO
        YearMonth yearMonth = this.argumentParseService.parseYearMonth(commandEvent.getArgs().get(0));
        double incomeSummary = this.argumentParseService.parsePositiveDouble(commandEvent.getArgs().get(1));
        double expenseSummary = this.argumentParseService.parsePositiveDouble(commandEvent.getArgs().get(2));

        Budget budget = new Budget();
        budget.setExpectedSummary(CategoryType.INCOME, incomeSummary);
        budget.setExpectedSummary(CategoryType.EXPENSE, expenseSummary);
        budget.setYearMonth(yearMonth);
        budget.setUser(commandEvent.getUser());

        budgetRepository.saveBudget(budget);

        commandEvent.getBot().sendMessage(commandEvent.getUser(), "Бюджет создан");
    }
}
