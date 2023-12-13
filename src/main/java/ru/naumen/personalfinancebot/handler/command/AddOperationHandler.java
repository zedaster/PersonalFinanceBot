package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.user.UserRepository;
import ru.naumen.personalfinancebot.service.ArgumentParseService;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды, которая добавляет операцию и отправляет пользователю сообщение
 *
 * @author Aleksandr Kornilov
 */
public class AddOperationHandler implements CommandHandler {
    /**
     * Тип категории, с которым будет работать обработчик
     */
    private final CategoryType categoryType;

    /**
     * Хранилище пользователей
     */
    private final UserRepository userRepository;

    /**
     * Хранилище категорий
     */
    private final CategoryRepository categoryRepository;

    /**
     * Хранилище операций
     */
    private final OperationRepository operationRepository;

    /**
     * Сервис, который парсит аргументы
     */
    private final ArgumentParseService argumentParser;

    public AddOperationHandler(CategoryType categoryType, UserRepository userRepository,
                               CategoryRepository categoryRepository, OperationRepository operationRepository, ArgumentParseService argumentParser) {
        this.categoryType = categoryType;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.operationRepository = operationRepository;
        this.argumentParser = argumentParser;
    }

    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(CommandData commandData, Session session) {
        if (commandData.getArgs().size() < 2) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_OPERATION_ARGS_AMOUNT);
            return;
        }
        Operation operation;
        try {
            operation = createOperationRecord(commandData.getUser(), commandData.getArgs(), categoryType, session);
        } catch (CategoryRepository.CategoryDoesNotExist e) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.CATEGORY_DOES_NOT_EXISTS);
            return;
        } catch (NumberFormatException e) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_PAYMENT_ARG);
            return;
        } catch (IllegalArgumentException e) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_CATEGORY_ARGUMENT_FORMAT);
            return;
        }
        double currentBalance = commandData.getUser().getBalance() + operation.getPayment();
        User user = commandData.getUser();
        user.setBalance(currentBalance);
        userRepository.saveUser(session, user);
        String message = categoryType == CategoryType.INCOME
                ? Message.ADD_INCOME_MESSAGE
                : Message.ADD_EXPENSE_MESSAGE;
        commandData.getBot().sendMessage(user,
                message + operation.getCategory().getCategoryName());

    }

    /**
     * Метод для записи в базу операции;
     *
     * @param user Пользователь
     * @param args Аргументы, переданные с командой
     * @param type Расход/Бюджет.
     * @return Совершенная операция
     */
    private Operation createOperationRecord(User user, List<String> args, CategoryType type, Session session)
            throws CategoryRepository.CategoryDoesNotExist {
        double payment = Double.parseDouble(args.get(0));
        if (payment <= 0) {
            throw new NumberFormatException();
        }
        String categoryName = this.argumentParser.parseCategory(args.subList(1, args.size()));
        if (type == CategoryType.EXPENSE) {
            payment = -Math.abs(payment);
        } else if (type == CategoryType.INCOME) {
            payment = Math.abs(payment);
        }
        Optional<Category> category = this.categoryRepository.getCategoryByName(session, user, type, categoryName);
        if (category.isEmpty()) {
            throw new CategoryRepository.CategoryDoesNotExist();
        }
        return this.operationRepository.addOperation(session, user, category.get(), payment);
    }
}
