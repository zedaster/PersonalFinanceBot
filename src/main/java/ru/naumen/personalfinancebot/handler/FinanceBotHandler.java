package ru.naumen.personalfinancebot.handler;

import com.sun.istack.Nullable;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Обработчик операций для бота "Персональный финансовый трекер"
 */
public class FinanceBotHandler implements BotHandler {
    /**
     * Коллекция, которая хранит обработчики для каждой команды
     */
    private final Map<String, Consumer<HandleCommandEvent>> handlers;

    private final UserRepository userRepository;
    private final OperationRepository operationRepository;
    private final CategoryRepository categoryRepository;

    public FinanceBotHandler(
            UserRepository userRepository,
            OperationRepository operationRepository,
            CategoryRepository categoryRepository
    ) {
        this.handlers = new HashMap<>();
        this.handlers.put("set_balance", this::handleSetBalance);
        this.handlers.put("add_expense", this::handleAddExpense);
        this.handlers.put("add_income", this::handleAddIncome);
        this.handlers.put("category_add", this::handleCategoryAdd);
        this.handlers.put("category_remove", this::handleCategoryRemove);
        this.handlers.put("category_list", this::handleCategoryList);
        // TODO Добавить больше обработчиков

        this.operationRepository = operationRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Вызывается при получении какой-либо команды от пользователя
     */
    @Override
    public void handleCommand(HandleCommandEvent event) {
        Consumer<HandleCommandEvent> handler = this.handlers.get(event.getCommandName().toLowerCase());
        if (handler != null) {
            handler.accept(event);
        } else {
            event.getBot().sendMessage(event.getUser(), "Команда не распознана...");
        }
        // TODO Действия при handler == null
    }

    /**
     * Команда для установки баланса
     */
    private void handleSetBalance(HandleCommandEvent event) {
        double amount;
        try {
            if (event.getArgs().size() != 1) throw new IllegalArgumentException();
            amount = parseBalanceAmount(event.getArgs().get(0));
        } catch (IllegalArgumentException e) {
            event.getBot().sendMessage(event.getUser(),
                    "Команда введена неверно! Введите /set_balance <новый баланс>");
            return;
        }

        event.getUser().setBalance(amount);
        userRepository.saveUser(event.getUser());
        event.getBot().sendMessage(event.getUser(), "Ваш баланс изменен. Теперь он составляет " +
                beautifyDouble(amount));
    }

    /**
     * Команда для добавления пользовательской категории
     */
    private void handleCategoryAdd(HandleCommandEvent event) {
        // TODO
//        Category category = new Category(event.getUser(), )
    }

    /**
     * Команда для удаления пользовательской категории
     */
    private void handleCategoryRemove(HandleCommandEvent event) {
        // TODO

    }

    /**
     * Команда для вывода категории пользователем
     */
    private void handleCategoryList(HandleCommandEvent event) {
        // TODO
    }

    /**
     * Парсит баланс, введенный пользователем
     * Вернет null если баланс не является числом с плавающей точкой или меньше нуля
     */
    @Nullable
    private Double parseBalanceAmount(String string) throws NumberFormatException {
        double amount = Double.parseDouble(string.replace(",", "."));
        if (amount < 0) throw new NumberFormatException();
        return amount;
    }

    /**
     * Форматирует double в красивую строку
     * Если число целое, то вернет его без дробной части.
     * Т.е. 1000.0 будет выведено как 1000
     * А 1000.99 будет выведено как 1000.99
     */
    private String beautifyDouble(double d) {
        if ((int) d == d) return String.valueOf((int) d);
        return String.valueOf(d);
    }

    /**
     * Команда для добавления трат
     * {@link HandleCommandEvent}
     */
    private void handleAddExpense(HandleCommandEvent event) {
        addOperation(event, CategoryType.EXPENSE);
    }

    /**
     * Обработчик для добавления доходов (команда /add_income)
     * {@link HandleCommandEvent}
     */
    private void handleAddIncome(HandleCommandEvent event) {
        addOperation(event, CategoryType.INCOME);
    }

    /**
     * Добавляет Операцию и отправляет пользователю сообщение
     *
     * @param event Event
     * @param type  Тип категории: Расход/доход
     */
    private void addOperation(HandleCommandEvent event, CategoryType type) {
        if (event.getArgs().size() != 2) {
            event.getBot().sendMessage(event.getUser(),StaticMessages.INCORRECT_ARGS_AMOUNT);
            return;
        }
        Operation operation = createOperationRecord(event.getUser(), event.getArgs(), type);
        if (operation == null) {
            event.getBot().sendMessage(event.getUser(), StaticMessages.CATEGORY_DOES_NOT_EXISTS);
            return;
        }
        double currentBalance = event.getUser().getBalance() + operation.getPayment();
        User user = event.getUser();
        user.setBalance(currentBalance);
        userRepository.saveUser(user);
        String message = type == CategoryType.INCOME
                ? StaticMessages.ADD_INCOME_MESSAGE
                : StaticMessages.ADD_EXPENSE_MESSAGE;
        event.getBot().sendMessage(user,
                message + operation.getCategory().getCategoryName());
    }

    /**
     * Команда для записи в базу операции;
     *
     * @param user Пользователь
     * @param args Аргументы, переданные с командой
     * @param type Расход/Бюджет.
     * @return Совершенная операция
     */
    private Operation createOperationRecord(User user, List<String> args, CategoryType type) {
        double payment;
        try {
            payment = Double.parseDouble(args.get(0));
        } catch (NumberFormatException exception) {
            return null;
        }
        String categoryName = args.get(1);
        if (type == CategoryType.EXPENSE) {
            payment = -Math.abs(payment);
        } else if (type == CategoryType.INCOME) {
            payment = Math.abs(payment);
        }
        Optional<Category> category = this.categoryRepository.getCategoryByName(categoryName);
        if (category.isEmpty()) {
            return null;
        }
        return this.operationRepository.addOperation(user, category.get(), payment);
    }
}
