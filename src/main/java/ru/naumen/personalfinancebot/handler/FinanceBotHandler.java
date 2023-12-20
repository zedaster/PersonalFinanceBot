package ru.naumen.personalfinancebot.handler;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.*;
import ru.naumen.personalfinancebot.handler.command.budget.*;
import ru.naumen.personalfinancebot.handler.command.report.EstimateReportHandler;
import ru.naumen.personalfinancebot.handler.command.report.ReportExpensesHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.repository.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.user.UserRepository;
import ru.naumen.personalfinancebot.service.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Обработчик операций для бота "Персональный финансовый трекер"
 */
public class FinanceBotHandler {
    /**
     * Сообщение, если пользователь передал неверную команду
     */
    private static final String COMMAND_NOT_FOUND = "Команда не распознана...";

    /**
     * Коллекция, которая хранит обработчики для команд
     */
    private final Map<String, CommandHandler> commandHandlers;

    /**
     * @param userRepository      Репозиторий для работы с пользователем
     * @param operationRepository Репозиторий для работы с операциями
     * @param categoryRepository  Репозиторий для работы с категориями
     */
    public FinanceBotHandler(UserRepository userRepository, OperationRepository operationRepository,
                             CategoryRepository categoryRepository, BudgetRepository budgetRepository) {
        CategoryParseService categoryParseService = new CategoryParseService();
        DateParseService dateParseService = new DateParseService();
        NumberParseService numberParseService = new NumberParseService();
        OutputNumberFormatService numberFormatService = new OutputNumberFormatService();
        OutputMonthFormatService monthFormatService = new OutputMonthFormatService();
        CategoryListService categoryListService = new CategoryListService(categoryRepository);
        ReportService reportService = new ReportService(operationRepository, monthFormatService, numberFormatService);

        commandHandlers = new HashMap<>();
        commandHandlers.put("start", new StartCommandHandler());
        commandHandlers.put("set_balance", new SetBalanceHandler(numberParseService, numberFormatService,
                userRepository));
        commandHandlers.put("add_expense", new AddOperationHandler(CategoryType.EXPENSE, userRepository,
                categoryRepository, operationRepository, categoryParseService));
        commandHandlers.put("add_income", new AddOperationHandler(CategoryType.INCOME, userRepository,
                categoryRepository, operationRepository, categoryParseService));
        commandHandlers.put("add_income_category", new AddCategoryHandler(CategoryType.INCOME, categoryRepository,
                categoryParseService));
        commandHandlers.put("add_expense_category", new AddCategoryHandler(CategoryType.EXPENSE, categoryRepository,
                categoryParseService));
        commandHandlers.put("remove_income_category", new RemoveCategoryHandler(CategoryType.INCOME,
                categoryRepository, categoryParseService));
        commandHandlers.put("remove_expense_category", new RemoveCategoryHandler(CategoryType.EXPENSE,
                categoryRepository, categoryParseService));
        commandHandlers.put("list_categories", new FullListCategoriesHandler(categoryListService));
        commandHandlers.put("list_income_categories", new SingleListCategoriesHandler(CategoryType.INCOME,
                categoryListService));
        commandHandlers.put("list_expense_categories", new SingleListCategoriesHandler(CategoryType.EXPENSE,
                categoryListService));
        commandHandlers.put("report_expense", new ReportExpensesHandler(reportService));

        commandHandlers.put("budget", new SingleBudgetHandler(budgetRepository, operationRepository,
                numberFormatService, monthFormatService));
        commandHandlers.put("budget_help", new HelpBudgetHandler());
        commandHandlers.put("budget_create", new CreateBudgetHandler(budgetRepository, operationRepository,
                dateParseService, numberParseService, numberFormatService, monthFormatService));
        commandHandlers.put("budget_set_income", new EditBudgetHandler(budgetRepository, numberParseService,
                dateParseService, numberFormatService, monthFormatService, CategoryType.INCOME));
        commandHandlers.put("budget_set_expenses", new EditBudgetHandler(budgetRepository, numberParseService,
                dateParseService, numberFormatService, monthFormatService, CategoryType.EXPENSE));
        commandHandlers.put("budget_list", new ListBudgetHandler(budgetRepository, operationRepository,
                dateParseService, numberFormatService, monthFormatService));

        commandHandlers.put("estimate_report", new EstimateReportHandler(dateParseService, reportService));
    }

    /**
     * Вызывается при получении какой-либо команды от пользователя
     */
    public void handleCommand(CommandData commandData, Session session) {
        CommandHandler handler = this.commandHandlers.get(commandData.getCommandName().toLowerCase());
        if (handler != null) {
            handler.handleCommand(commandData, session);
        } else {
            commandData.getBot().sendMessage(commandData.getUser(), COMMAND_NOT_FOUND);
        }
    }
}
