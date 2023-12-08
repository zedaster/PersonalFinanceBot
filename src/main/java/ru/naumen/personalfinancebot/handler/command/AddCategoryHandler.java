package ru.naumen.personalfinancebot.handler.command;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.service.ArgumentParseService;

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
            String responseText = Message.USER_CATEGORY_ALREADY_EXISTS
                    .replace("{type}", typeLabel)
                    .replace("{name}", categoryName);
            event.getBot().sendMessage(event.getUser(), responseText);
            return;
        } catch (CategoryRepository.CreatingExistingStandardCategoryException e) {
            String responseText = Message.STANDARD_CATEGORY_ALREADY_EXISTS
                    .replace("{type}", typeLabel)
                    .replace("{name}", categoryName);
            event.getBot().sendMessage(event.getUser(), responseText);
            return;
        }

        String responseText = Message.USER_CATEGORY_ADDED
                .replace("{type}", typeLabel)
                .replace("{name}", categoryName);
        event.getBot().sendMessage(event.getUser(), responseText);
    }
}
