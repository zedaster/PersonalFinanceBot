package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.Messages;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.services.ArgumentParseService;

/**
 * Обработчик команд для удаления пользовательской категории определенного типа
 *
 * @author Sergey Kazantsev
 */
public class RemoveCategoryHandler implements CommandHandler {
    /**
     * Хранилище категорий
     */
    private final CategoryRepository categoryRepository;

    /**
     * Тип категории, с которым будет работать обработчик
     */
    private final CategoryType categoryType;

    /**
     * Сервис, который парсит аргументы
     */
    private final ArgumentParseService argumentParser;

    public RemoveCategoryHandler(CategoryType categoryType, CategoryRepository categoryRepository, ArgumentParseService argumentParser) {
        this.categoryRepository = categoryRepository;
        this.categoryType = categoryType;
        this.argumentParser = argumentParser;
    }

    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(HandleCommandEvent event) {
        String typeLabel = categoryType.getPluralShowLabel();
        String categoryName;
        try {
            categoryName = argumentParser.parseCategory(event.getArgs());
        } catch (IllegalArgumentException ex) {
            event.getBot().sendMessage(event.getUser(), ex.getMessage());
            return;
        }

        try {
            categoryRepository.removeUserCategoryByName(event.getUser(), categoryType, categoryName);
        } catch (CategoryRepository.RemovingNonExistentCategoryException e) {
            String responseText = Messages.USER_CATEGORY_ALREADY_NOT_EXISTS
                    .replace("{type}", typeLabel)
                    .replace("{name}", categoryName);
            event.getBot().sendMessage(event.getUser(), responseText);
            return;
        }

        String responseText = Messages.USER_CATEGORY_REMOVED
                .replace("{type}", typeLabel)
                .replace("{name}", categoryName);
        event.getBot().sendMessage(event.getUser(), responseText);
    }
}
