package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.service.CategoryListService;

/**
 * Обработчик команды для вывода доходов или трат
 *
 * @author Sergey Kazantsev
 */
public class SingleListCategoriesHandler implements CommandHandler {
    /**
     * Сервис, который работает со списками категорий
     */
    private final CategoryListService categoryListService;
    /**
     * Тип категории, с которым будет работать обработчик
     */
    private final CategoryType categoryType;

    public SingleListCategoriesHandler(CategoryType categoryType, CategoryListService categoryListService) {
        this.categoryListService = categoryListService;
        this.categoryType = categoryType;
    }

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        String content = categoryListService.getListContent(session, commandData.getUser(), categoryType);
        commandData.getBot().sendMessage(commandData.getUser(), content);
    }
}
