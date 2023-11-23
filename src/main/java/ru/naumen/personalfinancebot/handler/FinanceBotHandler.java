package ru.naumen.personalfinancebot.handler;

import ru.naumen.personalfinancebot.handler.commands.*;
import ru.naumen.personalfinancebot.handler.commands.budget.*;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.repositories.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;
import ru.naumen.personalfinancebot.services.ArgumentParseService;
import ru.naumen.personalfinancebot.services.CategoryListService;
import ru.naumen.personalfinancebot.services.OutputFormatService;
import ru.naumen.personalfinancebot.services.ReportService;

import java.util.HashMap;
import java.util.Map;

/**
 * Обработчик операций для бота "Персональный финансовый трекер"
 */
public class FinanceBotHandler {
    /**
     * Коллекция, которая хранит обработчики для команд
     */
    private final Map<String, CommandHandler> commandHandlers;

    public FinanceBotHandler(
            UserRepository userRepository,
            OperationRepository operationRepository,
            CategoryRepository categoryRepository,
            BudgetRepository budgetRepository
    ) {
        ArgumentParseService argumentParseService = new ArgumentParseService();
        OutputFormatService outputFormatService = new OutputFormatService();
        CategoryListService categoryListService = new CategoryListService(categoryRepository);
        ReportService reportService = new ReportService(operationRepository);

        commandHandlers = new HashMap<>();
        commandHandlers.put("start", new StartCommandHandler());
        commandHandlers.put("set_balance", new SetBalanceHandler(argumentParseService, outputFormatService,
                userRepository));
        commandHandlers.put("add_expense", new AddOperationHandler(CategoryType.EXPENSE, userRepository,
                categoryRepository, operationRepository));
        commandHandlers.put("add_income", new AddOperationHandler(CategoryType.INCOME, userRepository,
                categoryRepository, operationRepository));
        commandHandlers.put("add_income_category", new AddCategoryHandler(CategoryType.INCOME, categoryRepository,
                argumentParseService));
        commandHandlers.put("add_expense_category", new AddCategoryHandler(CategoryType.EXPENSE, categoryRepository,
                argumentParseService));
        commandHandlers.put("remove_income_category", new RemoveCategoryHandler(CategoryType.INCOME,
                categoryRepository, argumentParseService));
        commandHandlers.put("remove_expense_category", new RemoveCategoryHandler(CategoryType.EXPENSE,
                categoryRepository, argumentParseService));
        commandHandlers.put("list_categories", new FullListCategoriesHandler(categoryListService));
        commandHandlers.put("list_income_categories", new SingleListCategoriesHandler(CategoryType.INCOME,
                categoryListService));
        commandHandlers.put("list_expense_categories", new SingleListCategoriesHandler(CategoryType.EXPENSE,
                categoryListService));
        commandHandlers.put("report_expense", new ReportExpensesHandler(reportService));

        commandHandlers.put("budget", new SingleBudgetHandler(budgetRepository, operationRepository,
                outputFormatService));
        commandHandlers.put("budget_help", new HelpBudgetHandler());
        commandHandlers.put("budget_create", new CreateBudgetHandler(budgetRepository, operationRepository,
                argumentParseService, outputFormatService));
        commandHandlers.put("budget_set_income", new EditBudgetHandler(budgetRepository,
                argumentParseService, outputFormatService, CategoryType.INCOME));
        commandHandlers.put("budget_set_expenses", new EditBudgetHandler(budgetRepository,
                argumentParseService, outputFormatService, CategoryType.EXPENSE));
        commandHandlers.put("budget_list", new ListBudgetHandler(budgetRepository, operationRepository,
                argumentParseService, outputFormatService));
    }

    /**
     * Вызывается при получении какой-либо команды от пользователя
     */
    public void handleCommand(HandleCommandEvent event) {
        CommandHandler handler = this.commandHandlers.get(event.getCommandName().toLowerCase());
        if (handler != null) {
            handler.handleCommand(event);
        } else {
            event.getBot().sendMessage(event.getUser(), StaticMessages.COMMAND_NOT_FOUND);
        }
    }
}
