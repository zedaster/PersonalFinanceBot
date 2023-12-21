package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingStandardCategoryException;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingUserCategoryException;
import ru.naumen.personalfinancebot.service.CategoryParseService;

/**
 * Обработчик команд для добавления пользовательской категории определенного типа
 *
 * @author Sergey Kazantsev
 */
public class AddCategoryHandler implements CommandHandler {
    /**
     * Сообщение о существовании персональной (пользовательской) категории
     */
    private static final String USER_CATEGORY_ALREADY_EXISTS = "Персональная категория %s '%s' уже существует.";

    /**
     * Сообщение о существовании стандартной категории
     */
    private static final String STANDARD_CATEGORY_ALREADY_EXISTS = "Стандартная категория %s '%s' уже " +
            "существует.";

    /**
     * Сообщение об успешно созданной пользовательской категории
     */
    private static final String USER_CATEGORY_ADDED = "Категория %s '%s' успешно добавлена";

    /**
     * Тип категории, с которым будет работать обработчик
     */
    private final CategoryType type;

    /**
     * Хранилище категорий
     */
    private final CategoryRepository categoryRepository;

    /**
     * Сервис, который парсит категорию
     */
    private final CategoryParseService categoryParseService;

    public AddCategoryHandler(CategoryType type, CategoryRepository categoryRepository, CategoryParseService categoryParseService) {
        this.type = type;
        this.categoryRepository = categoryRepository;
        this.categoryParseService = categoryParseService;
    }

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        String categoryName;
        try {
            categoryName = categoryParseService.parseCategory(commandData.getArgs());
        } catch (IllegalArgumentException ex) {
            commandData.getBot().sendMessage(commandData.getUser(), ex.getMessage());
            return;
        }

        String typeLabel = type.getPluralShowLabel();
        try {
            categoryRepository.createUserCategory(session, commandData.getUser(), type, categoryName);
        } catch (ExistingUserCategoryException e) {
            String responseText = USER_CATEGORY_ALREADY_EXISTS.formatted(typeLabel, categoryName);
            commandData.getBot().sendMessage(commandData.getUser(), responseText);
            return;
        } catch (ExistingStandardCategoryException e) {
            String responseText = STANDARD_CATEGORY_ALREADY_EXISTS.formatted(typeLabel, categoryName);
            commandData.getBot().sendMessage(commandData.getUser(), responseText);
            return;
        }

        String responseText = USER_CATEGORY_ADDED.formatted(typeLabel, categoryName);
        commandData.getBot().sendMessage(commandData.getUser(), responseText);
    }
}
