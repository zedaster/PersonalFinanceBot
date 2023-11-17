package ru.naumen.personalfinancebot.handler;

import com.sun.istack.Nullable;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;

import java.util.List;

public class OperationsTests {
    /**
     * Статическое поле для начального баланса
     */
    private static final double BALANCE = 100_000;

    /**
     * Репозиторий для работы с пользователем
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий для работы с категориями
     */
    private final CategoryRepository categoryRepository;

    /**
     * Обработчик операций в боте
     */
    private final FinanceBotHandler botHandler;


    public OperationsTests() {
        HibernateConfiguration hibernateUserRepository = new HibernateConfiguration();
        SessionFactory sessionFactory = hibernateUserRepository.getSessionFactory();

        this.userRepository = new HibernateUserRepository(sessionFactory);
        this.categoryRepository = new HibernateCategoryRepository(sessionFactory);
        OperationRepository operationRepository = new HibernateOperationRepository(sessionFactory);

        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository);
    }

    // Тесты, с созданием категории (Т.е. пользователь может добавить операцию по категории)

    /**
     * Корретное добавление дохода
     */
    @Test
    public void runCorrectIncomeTest() {
        String command = "add_income";
        String categoryName = "Зарплата";
        List<String> argumentsList = List.of("0", "-2343", "1000", "Не число", "Опять не число", "0.001");
        for (String payment : argumentsList) {
            testCorrectOperationAdding(command, payment, categoryName, CategoryType.INCOME);
        }
    }

    /**
     * Корретное добавление расхода
     */
    @Test
    public void runCorrectExpenseTest() {
        String command = "add_expense";
        String categoryName = "Такси";
        List<String> argumentsList = List.of("0", "1000", "Не число", "Опять не число", "0.001");
        for (String payment : argumentsList) {
            testCorrectOperationAdding(command, payment, categoryName, CategoryType.EXPENSE);
        }
    }

    /**
     * Тестирует правильно добавление операции
     * @param command Команда
     * @param payment Плата
     * @param categoryName Имя категории
     * @param type Тип категории
     */
    public void testCorrectOperationAdding(String command, String payment, String categoryName, CategoryType type) {
        User user = new User(1, BALANCE);
        this.userRepository.saveUser(user);

        Category category = this.createUserCategory(user, type, categoryName);
        Assert.assertEquals(categoryName, category.getCategoryName());

        MockBot bot = new MockBot();
        HandleCommandEvent commandEvent = new HandleCommandEvent(bot, user, command, List.of(payment, categoryName));
        this.botHandler.handleCommand(commandEvent);

        int messagesAmount = bot.getMessageQueueSize();
        Assert.assertEquals(1, messagesAmount);

        MockMessage message = bot.poolMessageQueue();
        Assert.assertEquals(user.getId(), message.receiver().getId());

        double expectedBalance = getBeautifyPayment(payment, type) + BALANCE;
        Assert.assertEquals(expectedBalance, user.getBalance(), 1e-3);
        this.clear(user.getId(), category.getId());
    }

    // Тесты, без создания категории (Т.е. ошибка во время добавления операции)

    /**
     * Запускает тесты без категории на добавление дохода
     */
    @Test
    public void runIncorrectIncomeTest() {
        String command = "add_income";
        String categoryName = "Переводы";
        List<String> argumentsList = List.of("300", "2343", "1000", "0.001");
        for (String payment : argumentsList)
            testWithoutCategory(command, categoryName, payment);
    }

    /**
     * Запускает тесты без категории на добавление расхода
     */
    @Test
    public void runIncorrectExpenseTest() {
        String command = "add_expense";
        String categoryName = "Химчистка";
        List<String> argumentsList = List.of("300", "2343", "1000", "0.001");
        for (String payment : argumentsList)
            testWithoutCategory(command, categoryName, payment);
    }

    /**
     * Тестирует добавление операции без создания категории
     * @param command Команда
     * @param categoryName Название категории
     * @param payment Плата
     */
    private void testWithoutCategory(String command, String categoryName, String payment) {
        User user = new User(1, BALANCE);
        this.userRepository.saveUser(user);

        MockBot bot = new MockBot();
        HandleCommandEvent commandEvent = new HandleCommandEvent(bot, user, command, List.of(payment, categoryName));
        this.botHandler.handleCommand(commandEvent);

        Assert.assertEquals(1, bot.getMessageQueueSize());
        MockMessage message = bot.poolMessageQueue();

        Assert.assertEquals(StaticMessages.CATEGORY_DOES_NOT_EXISTS, message.text());
        Assert.assertEquals(user.getId(), message.receiver().getId());
        Assert.assertEquals(BALANCE, user.getBalance(), 0);
        clear(user.getId(), null);
    }

    // Тесты с неверным количеством аргументов

    /**
     * Запускает тесты на неверное кол-во аргументов
     */
    @Test
    public void incorrectNumberOfArgumentsExpenseTest() {
        String command = "add_expense";
        List<List<String>> argumentsList = List.of(
                List.of(),
                List.of("alone arg"),
                List.of("300", "Такси", "335242", "ЧЕТЫРЕ АРГУМЕНТА"),
                List.of("300", "Полная чушь", "335242", "О БОЖЕ ПЯТЬ АРГУМЕНТОВ")
        );
        for (List<String> arguments : argumentsList) {
            assertArgsAmountError(command, arguments);
        }
    }

    /**
     * Запускает тесты на добавление дохода с неверным количеством аргументов
     */
    @Test
    public void incorrectNumberOfArgumentsIncomeTest() {
        String command = "add_income";
        List<List<String>> argumentsList = List.of(
                List.of(),
                List.of("alone arg"),
                List.of("300", "Такси", "335242", "ЧЕТЫРЕ АРГУМЕНТА"),
                List.of("300", "Полная чушь", "335242", "О БОЖЕ ПЯТЬ АРГУМЕНТОВ")
        );
        for (List<String> arguments : argumentsList) {
            assertArgsAmountError(command, arguments);
        }
    }

    /**
     * Тестирует на кол-во переданных аргументов
     * @param command Команда
     * @param args Аргументы
     */
    public void assertArgsAmountError(String command, List<String> args) {
        User user = new User(1, BALANCE);
        this.userRepository.saveUser(user);

        MockBot bot = new MockBot();
        HandleCommandEvent commandEvent = new HandleCommandEvent(bot, user, command, args);
        this.botHandler.handleCommand(commandEvent);
        MockMessage message = bot.poolMessageQueue();
        Assert.assertEquals(StaticMessages.INCORRECT_OPERATION_ARGS_AMOUNT, message.text());
        clear(user.getId(), null);
    }

    /**
     * Тест для команды /add_income с неположительным аргументом {payment}
     */
    @Test
    public void negativePaymentWithIncomeOperation() {
        String command = "add_income";
        String categoryName = "Зарплата";
        List<String> payments = List.of("-100", "-0.1", "-12435523", "0");
        for (String payment : payments) {
            testIllegalArgument(command, categoryName, payment, CategoryType.INCOME);
        }
    }

    /**
     * Общий тест для методов с аргументами не тех знаков.
     * @param command Команда
     * @param categoryName Название категории
     * @param payment Плата
     * @param type Тип операции
     */
    public void testIllegalArgument(String command, String categoryName, String payment, CategoryType type) {
        User user = new User(1, BALANCE);
        this.userRepository.saveUser(user);

        Category category = this.createUserCategory(user, type, categoryName);
        Assert.assertEquals(category.getCategoryName(), categoryName);

        MockBot bot = new MockBot();
        HandleCommandEvent commandEvent = new HandleCommandEvent(bot, user, command, List.of(payment, categoryName));
        this.botHandler.handleCommand(commandEvent);

        MockMessage message = bot.poolMessageQueue();
        Assert.assertEquals(StaticMessages.ILLEGAL_PAYMENT_ARGUMENT, message.text());
        Assert.assertEquals(BALANCE, user.getBalance(), 0);
        clear(user.getId(), category.getId());
    }

    /**
     * Метод очищает таблицы пользователей,
     * @param userId ID пользователя
     * @param categoryId ID категории
     */
    public void clear(Long userId, @Nullable Long categoryId) {
        this.userRepository.removeUserById(userId);
        if (categoryId != null) {
            try {
                this.categoryRepository.removeCategoryById(categoryId);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Создает и возвращает категорию
     *
     * @param user         Пользователь
     * @param type         Тип категории
     * @param categoryName Название категории
     * @return Созданная Категория
     */
    private Category createUserCategory(User user, CategoryType type, String categoryName) {
        try {
            return this.categoryRepository.createUserCategory(user, type, categoryName);
        } catch (CategoryRepository.CreatingExistingUserCategoryException |
                 CategoryRepository.CreatingExistingStandardCategoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Парсит переданное число к типу double, в случае исключения возвращает 0
     *
     * @param payment Число в виде строки
     * @param type    Тип категории
     * @return Число
     */
    private double getBeautifyPayment(String payment, CategoryType type) {
        double result;
        try {
            result = Double.parseDouble(payment);
        } catch (NumberFormatException e) {
            return 0.0;
        }
        if (CategoryType.INCOME  == type && result > 0) {
            return result;
        }
        if (CategoryType.EXPENSE == type && result > 0) {
            return - result;
        }
        return 0.0;
    }
}