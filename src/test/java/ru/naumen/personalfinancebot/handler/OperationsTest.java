package ru.naumen.personalfinancebot.handler;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.TransactionManager;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repository.user.UserRepository;

import java.util.List;

public class OperationsTest {
    /**
     * Статическое поле для начального баланса
     */
    private final double BALANCE = 100_000;

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

    /**
     * Менеджер транзакций
     */
    private final TransactionManager transactionManager;


    public OperationsTest() {
        HibernateConfiguration hibernateConfiguration = new HibernateConfiguration();
        this.transactionManager = new TransactionManager(hibernateConfiguration.getSessionFactory());
        SessionFactory sessionFactory = hibernateConfiguration.getSessionFactory();
        this.userRepository = new HibernateUserRepository();
        this.categoryRepository = new HibernateCategoryRepository();
        OperationRepository operationRepository = new HibernateOperationRepository();

        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository, sessionFactory);
    }

    /**
     * Создает пользователя для проведения тестов
     */
    private User createUser(Session session) {
        User user = new User(1L, this.BALANCE);
        this.userRepository.saveUser(session, user);
        return user;
    }

    /**
     * Проверяет, что доход добавится при условии что категория существует.
     */
    @Test
    public void testIncomeOperationIfCategoryExists() {
        transactionManager.produceTransaction(session -> {
            User user = createUser(session);
            MockBot bot = new MockBot();
            CommandData commandCategoryAdd = new CommandData(
                    bot, user, "add_income_category", List.of("Зарплата"));
            this.botHandler.handleCommand(commandCategoryAdd, session);
            bot.poolMessageQueue();

            CommandData commandAddIncome = new CommandData(
                    bot, user, "add_income", List.of("2000", "Зарплата"));
            this.botHandler.handleCommand(commandAddIncome, session);
            MockMessage message = bot.poolMessageQueue();

            Assert.assertEquals(102_000, user.getBalance(), 1e-10);
            Assert.assertEquals(
                    "Вы успешно добавили доход по источнику: Зарплата",
                    message.text()
            );
        });
    }

    /**
     * Тест для добавления операции по категории, состоящей из нескольких слов
     */
    @Test
    public void testCategoryOfManyWords() {
        List<List<String>> argumentsList = List.of(
                List.of("100", "Заработная", "плата"),
                List.of("200", "Жилищно", "Коммунальные", "Услуги")
        );
        transactionManager.produceTransaction(session -> {
            User user = createUser(session);
            MockBot bot = new MockBot();

            CommandData commandDataIncomeCategory = new CommandData(
                    bot, user, "add_income_category", List.of("Заработная", "плата")
            );
            CommandData commandDataExpenseCategory = new CommandData(
                    bot, user, "add_expense_category", List.of("Жилищно", "Коммунальные", "Услуги")
            );

            this.botHandler.handleCommand(commandDataIncomeCategory, session);
            System.out.println(bot.poolMessageQueue().text());
            this.botHandler.handleCommand(commandDataExpenseCategory, session);
            bot.poolMessageQueue();

            CommandData commandDataIncome = new CommandData(bot, user, "add_income", argumentsList.get(0));
            this.botHandler.handleCommand(commandDataIncome, session);
            MockMessage messageIncomeOperation = bot.poolMessageQueue();
            Assert.assertEquals(100_100, user.getBalance(), 1e-10);
            Assert.assertEquals(
                    "Вы успешно добавили доход по источнику: Заработная плата", messageIncomeOperation.text()
            );

            CommandData commandDataExpense = new CommandData(bot, user, "add_expense", argumentsList.get(1));
            this.botHandler.handleCommand(commandDataExpense, session);
            MockMessage messageExpenseOperation = bot.poolMessageQueue();
            Assert.assertEquals(99_900, user.getBalance(), 1e-10);
            Assert.assertEquals(
                    "Добавлен расход по категории: Жилищно коммунальные услуги", messageExpenseOperation.text()
            );
        });
    }

    /**
     * Проверяет, что расход добавится при условии что категория существует.
     */
    @Test
    public void testExpenseOperationIfCategoryExists() {
        transactionManager.produceTransaction(session -> {
            User user = createUser(session);
            MockBot bot = new MockBot();
            CommandData commandDataCategoryAdd = new CommandData(
                    bot, user, "add_expense_category", List.of("Такси"));
            this.botHandler.handleCommand(commandDataCategoryAdd, session);
            bot.poolMessageQueue();

            CommandData commandAddIncome = new CommandData(
                    bot, user, "add_expense", List.of("2000", "Такси"));
            this.botHandler.handleCommand(commandAddIncome, session);
            MockMessage message = bot.poolMessageQueue();

            Assert.assertEquals(98_000, user.getBalance(), 1e-10);
            Assert.assertEquals("Добавлен расход по категории: Такси", message.text());
        });
    }

    /**
     * Проверяет, что доход не добавится, так как категории не существует.
     */
    @Test
    public void testIncomeOperationIfCategoryNotExists() {
        transactionManager.produceTransaction(session -> {
            User user = createUser(session);
            MockBot bot = new MockBot();
            CommandData commandAddIncome = new CommandData(
                    bot, user, "add_expense", List.of("2000", "Стипендия"));
            this.botHandler.handleCommand(commandAddIncome, session);
            MockMessage message = bot.poolMessageQueue();

            Assert.assertEquals(100_000, user.getBalance(), 1e-10);
            Assert.assertEquals(
                    "Указанная категория не числится. Используйте команду /add_[income/expense]_category чтобы добавить её",
                    message.text()
            );
        });
    }

    /**
     * Проверяет, что расход не добавится, так как категории не существует.
     */
    @Test
    public void testExpenseOperationIfCategoryNotExists() {
        transactionManager.produceTransaction(session -> {
            User user = createUser(session);
            MockBot bot = new MockBot();
            CommandData commandAddIncome = new CommandData(
                    bot, user, "add_income", List.of("2000", "Такси"));
            this.botHandler.handleCommand(commandAddIncome, session);
            MockMessage message = bot.poolMessageQueue();

            Assert.assertEquals(100_000, user.getBalance(), 1e-10);
            Assert.assertEquals(
                    "Указанная категория не числится. Используйте команду /add_[income/expense]_category чтобы добавить её",
                    message.text()
            );
        });
    }

    /**
     * Метод проверяет количество переданных аргументов.
     */
    @Test
    public void testOperationIncorrectArgumentCount() {
        transactionManager.produceTransaction(session -> {
            User user = createUser(session);
            MockBot bot = new MockBot();

            List<List<String>> argumentsList = List.of(List.of(), List.of("1аргумент"));
            String expected = "Данная команда принимает 2 аргумента: [payment - сумма] [категория расхода/дохода]";
            testPattern(session, "add_income", bot, expected, argumentsList, user);
            testPattern(session, "add_expense", bot, expected, argumentsList, user);
            Assert.assertEquals(100_000, user.getBalance(), 1e-10);
        });
    }

    /**
     * Проверка на неположительную сумму операции
     */
    @Test
    public void testPositivePaymentValue() {
        transactionManager.produceTransaction(session -> {
            User user = createUser(session);
            MockBot bot = new MockBot();
            List<List<String>> argumentsList = List.of(
                    List.of("0", "Зарплата"),
                    List.of("-1", "Зарплата"),
                    List.of(String.valueOf(Double.NEGATIVE_INFINITY), "Зарплата")
            );
            String expected = "Ошибка! Аргумент [payment] должен быть больше 0";
            testPattern(session, "add_income", bot, expected, argumentsList, user);
            testPattern(session, "add_expense", bot, expected, argumentsList, user);
            Assert.assertEquals(100_000, user.getBalance(), 1e-10);
        });
    }

    /**
     * Тест с неправильным форматом суммы операции
     */
    @Test
    public void testIncorrectPaymentFormat() {
        transactionManager.produceTransaction(session -> {
            User user = createUser(session);
            MockBot bot = new MockBot();
            List<List<String>> argumentsList = List.of(
                    List.of("неЧисло", "Зарплата"), List.of(".-1", "Зарплата"),
                    List.of("Опять не число", "Зарплата"), List.of("11111111111111111111.привет", "Зарплата")
            );
            String expected = "Сумма операции указана в неверном формате.";
            testPattern(session, "add_income", bot, expected, argumentsList, user);
            testPattern(session, "add_expense", bot, expected, argumentsList, user);
            Assert.assertEquals(100_000, user.getBalance(), 1e-10);
        });
    }


    /**
     * Делегирующий метод для итерации по списку аргументов и проверки ожидаемого значения
     *
     * @param command   Команда
     * @param bot       Моковый бот
     * @param expected  Ожидаемое значение
     * @param arguments Список аргументов
     * @param user      Пользователь
     */
    private void testPattern(Session session, String command, MockBot bot, String expected, List<List<String>> arguments, User user) {
        for (List<String> args : arguments) {
            CommandData commandAddIncome = new CommandData(bot, user, command, args);
            this.botHandler.handleCommand(commandAddIncome, session);
            MockMessage message = bot.poolMessageQueue();
            Assert.assertEquals(expected, message.text());
        }
    }
}