package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.services.CategoryListService;

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

    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(HandleCommandEvent event) {
        String content = categoryListService.getListContent(event.getUser(), categoryType);
        event.getBot().sendMessage(event.getUser(), content);
    }
}
