package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.services.CategoryListService;

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

    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(HandleCommandEvent event) {
        String incomeContent = categoryListService.getListContent(event.getUser(), CategoryType.INCOME);
        String expenseContent = categoryListService.getListContent(event.getUser(), CategoryType.EXPENSE);
        event.getBot().sendMessage(event.getUser(), incomeContent + "\n" + expenseContent);
    }
}
