package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.service.CategoryListService;

/**
 * Обработчик команды для вывода как доходов, так и трат
 *
 * @author Sergey Kazantsev
 */
public class FullListCategoriesHandler implements CommandHandler {
    /**
     * Сервис, который работает со списками категорий
     */
    private final CategoryListService categoryListService;

    public FullListCategoriesHandler(CategoryListService categoryListService) {
        this.categoryListService = categoryListService;
    }

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        String incomeContent = categoryListService.getListContent(session, commandData.getUser(), CategoryType.INCOME);
        String expenseContent = categoryListService.getListContent(session, commandData.getUser(), CategoryType.EXPENSE);
        commandData.getBot().sendMessage(commandData.getUser(), incomeContent + "\n" + expenseContent);
    }
}
