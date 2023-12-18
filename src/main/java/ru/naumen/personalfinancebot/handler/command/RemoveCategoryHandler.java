package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.category.exception.NotExistingCategoryException;
import ru.naumen.personalfinancebot.service.CategoryParseService;

/**
 * Обработчик команд для удаления пользовательской категории определенного типа
 *
 * @author Sergey Kazantsev
 */
public class RemoveCategoryHandler implements CommandHandler {
    /**
     * Сообщение об отсутствии пользовательской категории
     */
    private static final String USER_CATEGORY_ALREADY_NOT_EXISTS = "Пользовательской категории %s '%s' не " +
            "существует!";

    /**
     * Сообщение об успешном удалении пользовательской категориии
     */
    private static final String USER_CATEGORY_REMOVED = "Категория %s '%s' успешно удалена";

    /**
     * Хранилище категорий
     */
    private final CategoryRepository categoryRepository;

    /**
     * Тип категории, с которым будет работать обработчик
     */
    private final CategoryType categoryType;

    /**
     * Сервис, который парсит категорию
     */
    private final CategoryParseService categoryParseService;

    public RemoveCategoryHandler(CategoryType categoryType, CategoryRepository categoryRepository, CategoryParseService categoryParseService) {
        this.categoryRepository = categoryRepository;
        this.categoryType = categoryType;
        this.categoryParseService = categoryParseService;
    }

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        String typeLabel = categoryType.getPluralShowLabel();
        String categoryName;
        try {
            categoryName = categoryParseService.parseCategory(commandData.getArgs());
        } catch (IllegalArgumentException ex) {
            commandData.getBot().sendMessage(commandData.getUser(), ex.getMessage());
            return;
        }

        try {
            categoryRepository.removeUserCategoryByName(session, commandData.getUser(), categoryType, categoryName);
        } catch (NotExistingCategoryException e) {
            String responseText = USER_CATEGORY_ALREADY_NOT_EXISTS.formatted(typeLabel, categoryName);
            commandData.getBot().sendMessage(commandData.getUser(), responseText);
            return;
        }

        String responseText = USER_CATEGORY_REMOVED.formatted(typeLabel, categoryName);
        commandData.getBot().sendMessage(commandData.getUser(), responseText);
    }
}
