package ru.naumen.personalfinancebot.handler;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.handler.command.*;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.user.UserRepository;
import ru.naumen.personalfinancebot.service.ArgumentParseService;
import ru.naumen.personalfinancebot.service.CategoryListService;
import ru.naumen.personalfinancebot.service.ReportService;

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

    /**
     * Фабрика сессий к БД
     */
    private final SessionFactory sessionFactory;

    /**
     * @param userRepository      Репозиторий для работы с пользователем
     * @param operationRepository Репозиторий для работы с операциями
     * @param categoryRepository  Репозиторий для работы с категориями
     * @param sessionFactory      Фабрика сессий
     */
    public FinanceBotHandler(UserRepository userRepository, OperationRepository operationRepository, CategoryRepository categoryRepository, SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        ArgumentParseService argumentParseService = new ArgumentParseService();
        CategoryListService categoryListService = new CategoryListService(categoryRepository);
        ReportService reportService = new ReportService(operationRepository);

        commandHandlers = new HashMap<>();
        commandHandlers.put("start", new StartCommandHandler());
        commandHandlers.put("set_balance", new SetBalanceHandler(argumentParseService, userRepository));
        commandHandlers.put("add_expense", new AddOperationHandler(CategoryType.EXPENSE, userRepository, categoryRepository, operationRepository, argumentParseService));
        commandHandlers.put("add_income", new AddOperationHandler(CategoryType.INCOME, userRepository, categoryRepository, operationRepository, argumentParseService));
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
    }

    /**
     * Вызывается при получении какой-либо команды от пользователя
     */
    public void handleCommand(CommandData commandData, Session session) {
        CommandHandler handler = this.commandHandlers.get(commandData.getCommandName().toLowerCase());
        if (handler != null) {
            handler.handleCommand(commandData, session);
        } else {
            commandData.getBot().sendMessage(commandData.getUser(), Message.COMMAND_NOT_FOUND);
        }
    }
}
