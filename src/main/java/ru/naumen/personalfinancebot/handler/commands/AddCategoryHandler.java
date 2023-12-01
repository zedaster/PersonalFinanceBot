package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.Messages;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.services.ArgumentParseService;

/**
 * Обработчик команд для добавления пользовательской категории определенного типа
 *
 * @author Sergey Kazantsev
 */
public class AddCategoryHandler implements CommandHandler {
    /**
     * Тип категории, с которым будет работать обработчик
     */
    private final CategoryType type;

    /**
     * Хранилище категорий
     */
    private final CategoryRepository categoryRepository;

    /**
     * Сервис, который парсит аргументы
     */
    private final ArgumentParseService argumentParser;

    public AddCategoryHandler(CategoryType type, CategoryRepository categoryRepository, ArgumentParseService argumentParser) {
        this.type = type;
        this.categoryRepository = categoryRepository;
        this.argumentParser = argumentParser;
    }

    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(HandleCommandEvent event) {
        String categoryName;
        try {
            categoryName = argumentParser.parseCategory(event.getArgs());
        } catch (IllegalArgumentException ex) {
            event.getBot().sendMessage(event.getUser(), ex.getMessage());
            return;
        }

        String typeLabel = type.getPluralShowLabel();
        try {
            categoryRepository.createUserCategory(event.getUser(), type, categoryName);
        } catch (CategoryRepository.CreatingExistingUserCategoryException e) {
            String responseText = Messages.USER_CATEGORY_ALREADY_EXISTS
                    .replace("{type}", typeLabel)
                    .replace("{name}", categoryName);
            event.getBot().sendMessage(event.getUser(), responseText);
            return;
        } catch (CategoryRepository.CreatingExistingStandardCategoryException e) {
            String responseText = Messages.STANDARD_CATEGORY_ALREADY_EXISTS
                    .replace("{type}", typeLabel)
                    .replace("{name}", categoryName);
            event.getBot().sendMessage(event.getUser(), responseText);
            return;
        }

        String responseText = Messages.USER_CATEGORY_ADDED
                .replace("{type}", typeLabel)
                .replace("{name}", categoryName);
        event.getBot().sendMessage(event.getUser(), responseText);
    }
}
