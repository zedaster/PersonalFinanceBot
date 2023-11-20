package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.repositories.budget.BudgetRepository;
import ru.naumen.personalfinancebot.services.ArgumentParseService;

import java.time.YearMonth;
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
    private final ArgumentParseService argumentParseService;

    /**
     * Тип, значение которого нужно изменить в записи бюджета
     */
    private final CategoryType type;

    /**
     * @param budgetRepository     Репозиторий для работы с бюджетом
     * @param argumentParseService Сервис, который парсит аргументы
     * @param type                 Тип, значение которого нужно изменить в записи бюджета
     */
    public EditBudgetHandler(
            BudgetRepository budgetRepository, ArgumentParseService argumentParseService, CategoryType type) {
        this.budgetRepository = budgetRepository;
        this.argumentParseService = argumentParseService;
        this.type = type;
    }

    /**
     * Метод, вызываемый при получении команды
     *
     * @param commandEvent
     */
    @Override
    public void handleCommand(HandleCommandEvent commandEvent) {
        int argsCount = commandEvent.getArgs().size();
        YearMonth yearMonth = null;
        double amount;
        if (argsCount == 1) {
            amount = this.argumentParseService.parsePositiveDouble(commandEvent.getArgs().get(0));
        } else if (argsCount == 2) {
            yearMonth = this.argumentParseService.parseYearMonth(commandEvent.getArgs().get(0));
            amount = this.argumentParseService.parsePositiveDouble(commandEvent.getArgs().get(1));
        } else {
            // TODO Вывести сообщение о неверной ошибке
            return;
        }
        Optional<Budget> budget = this.budgetRepository.getBudget(commandEvent.getUser(), yearMonth);
        if (budget.isEmpty()) {
            // TODO: Баланс пользователя не установлен
            return;
        }
        budget.get().setExpectedSummary(this.type, amount);
        this.budgetRepository.saveBudget(budget.get());
        commandEvent.getBot().sendMessage(commandEvent.getUser(), "Сообщение о том, что что-то там поменялось...");
    }
}
