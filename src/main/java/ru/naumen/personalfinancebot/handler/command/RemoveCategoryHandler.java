package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.category.exception.RemovingNonExistentCategoryException;
import ru.naumen.personalfinancebot.service.ArgumentParseService;

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
    public void handleCommand(CommandData commandData, Session session) {
        String typeLabel = categoryType.getPluralShowLabel();
        String categoryName;
        try {
            categoryName = argumentParser.parseCategory(commandData.getArgs());
        } catch (IllegalArgumentException ex) {
            commandData.getBot().sendMessage(commandData.getUser(), ex.getMessage());
            return;
        }

        try {
            categoryRepository.removeUserCategoryByName(session, commandData.getUser(), categoryType, categoryName);
        } catch (RemovingNonExistentCategoryException e) {
            String responseText = Message.USER_CATEGORY_ALREADY_NOT_EXISTS
                    .replace("{type}", typeLabel)
                    .replace("{name}", categoryName);
            commandData.getBot().sendMessage(commandData.getUser(), responseText);
            return;
        }

        String responseText = Message.USER_CATEGORY_REMOVED
                .replace("{type}", typeLabel)
                .replace("{name}", categoryName);
        commandData.getBot().sendMessage(commandData.getUser(), responseText);
    }
}
