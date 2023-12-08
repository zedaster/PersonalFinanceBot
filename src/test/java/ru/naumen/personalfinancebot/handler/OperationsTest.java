package ru.naumen.personalfinancebot.handler;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.model.User;
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


    public OperationsTest() {
        HibernateConfiguration hibernateUserRepository = new HibernateConfiguration();
        SessionFactory sessionFactory = hibernateUserRepository.getSessionFactory();
        this.userRepository = new HibernateUserRepository(sessionFactory);
        this.categoryRepository = new HibernateCategoryRepository(sessionFactory);
        OperationRepository operationRepository = new HibernateOperationRepository(sessionFactory);

        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository);
    }

    public User createUser() {
        User user = new User(1L, this.BALANCE);
        this.userRepository.saveUser(user);
        return user;
    }

    /**
     * Проверяет, что доход добавится при условии что категория существует.
     */
    @Test
    public void testIncomeOperationIfCategoryExists() {
        User user = createUser();
        MockBot bot = new MockBot();
        HandleCommandEvent commandEventCategoryAdd = new HandleCommandEvent(
                bot, user, "add_income_category", List.of("Зарплата"));
        this.botHandler.handleCommand(commandEventCategoryAdd);
        bot.poolMessageQueue();

        HandleCommandEvent commandAddIncome = new HandleCommandEvent(
                bot, user, "add_income", List.of("2000", "Зарплата"));
        this.botHandler.handleCommand(commandAddIncome);
        MockMessage message = bot.poolMessageQueue();

        Assert.assertEquals(102_000, user.getBalance(), 1e-10);
        Assert.assertEquals(
                "Вы успешно добавили доход по источнику: Зарплата",
                message.text()
        );
    }

    /**
     * Проверяет, что расход добавится при условии что категория существует.
     */
    @Test
    public void testExpenseOperationIfCategoryExists() {
        User user = createUser();
        MockBot bot = new MockBot();
        HandleCommandEvent commandEventCategoryAdd = new HandleCommandEvent(
                bot, user, "add_expense_category", List.of("Такси"));
        this.botHandler.handleCommand(commandEventCategoryAdd);
        bot.poolMessageQueue();

        HandleCommandEvent commandAddIncome = new HandleCommandEvent(
                bot, user, "add_expense", List.of("2000", "Такси"));
        this.botHandler.handleCommand(commandAddIncome);
        MockMessage message = bot.poolMessageQueue();

        Assert.assertEquals(98_000, user.getBalance(), 1e-10);
        Assert.assertEquals("Добавлен расход по категории: Такси", message.text());
    }

    /**
     * Проверяет, что доход не добавится, так как категории не существует.
     */
    @Test
    public void testIncomeOperationIfCategoryNotExists() {
        User user = createUser();
        MockBot bot = new MockBot();
        HandleCommandEvent commandAddIncome = new HandleCommandEvent(
                bot, user, "add_expense", List.of("2000", "Стипендия"));
        this.botHandler.handleCommand(commandAddIncome);
        MockMessage message = bot.poolMessageQueue();

        Assert.assertEquals(100_000, user.getBalance(), 1e-10);
        Assert.assertEquals(
                "Указанная категория не числится. Используйте команду /add_[income/expense]_category чтобы добавить её",
                message.text()
        );
    }

    /**
     * Проверяет, что расход не добавится, так как категории не существует.
     */
    @Test
    public void testExpenseOperationIfCategoryNotExists() {
        User user = createUser();
        MockBot bot = new MockBot();
        HandleCommandEvent commandAddIncome = new HandleCommandEvent(
                bot, user, "add_income", List.of("2000", "Такси"));
        this.botHandler.handleCommand(commandAddIncome);
        MockMessage message = bot.poolMessageQueue();

        Assert.assertEquals(100_000, user.getBalance(), 1e-10);
        Assert.assertEquals(
                "Указанная категория не числится. Используйте команду /add_[income/expense]_category чтобы добавить её",
                message.text()
        );
    }

    /**
     * Метод проверяет количество переданных аргументов.
     */
    @Test
    public void testOperationIncorrectArgumentCount() {
        User user = createUser();
        MockBot bot = new MockBot();

        List<List<String>> argumentsList = List.of(
                List.of(), List.of("1аргумент"), List.of("1аргумент", "2аргумента", "3аргумента")
        );
        String expected = "Данная команда принимает 2 аргумента: [payment - сумма] [категория расхода/дохода]";
        testPattern("add_income", bot, expected, argumentsList, user);
        testPattern("add_expense", bot, expected, argumentsList, user);
        Assert.assertEquals(100_000, user.getBalance(), 1e-10);
    }

    /**
     * Проверка на неположительную сумму операции
     */
    @Test
    public void testPositivePaymentValue() {
        User user = createUser();
        MockBot bot = new MockBot();
        List<List<String>> argumentsList = List.of(
                List.of("0", "Зарплата"),
                List.of("-1", "Зарплата"),
                List.of(String.valueOf(Double.NEGATIVE_INFINITY), "Зарплата")
        );
        String expected = "Ошибка! Аргумент [payment] должен быть больше 0";
        testPattern("add_income", bot, expected, argumentsList, user);
        testPattern("add_expense", bot, expected, argumentsList, user);
        Assert.assertEquals(100_000, user.getBalance(), 1e-10);
    }

    /**
     * Тест с неправильным форматом суммы операции
     */
    @Test
    public void testIncorrectPaymentFormat() {
        User user = createUser();
        MockBot bot = new MockBot();
        List<List<String>> argumentsList = List.of(
                List.of("неЧисло", "Зарплата"), List.of(".-1", "Зарплата"),
                List.of("Опять не число", "Зарплата"), List.of("11111111111111111111.привет", "Зарплата")
        );
        String expected = "Сумма операции указана в неверном формате.";
        testPattern("add_income", bot, expected, argumentsList, user);
        testPattern("add_expense", bot, expected, argumentsList, user);
        Assert.assertEquals(100_000, user.getBalance(), 1e-10);
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
    private void testPattern(String command, MockBot bot, String expected, List<List<String>> arguments, User user) {
        for (List<String> args : arguments) {
            HandleCommandEvent commandAddIncome = new HandleCommandEvent(bot, user, command, args);
            this.botHandler.handleCommand(commandAddIncome);
            MockMessage message = bot.poolMessageQueue();
            Assert.assertEquals(expected, message.text());
        }
    }
}