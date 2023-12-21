package ru.naumen.personalfinancebot.handler.command.report;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.FinanceBotHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.ClearQueryManager;
import ru.naumen.personalfinancebot.repository.TransactionManager;
import ru.naumen.personalfinancebot.repository.budget.HibernateBudgetRepository;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingStandardCategoryException;
import ru.naumen.personalfinancebot.repository.operation.FakeDatedOperationRepository;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repository.user.UserRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Интеграционный тест для команды /avg_report
 * Случаи, когда пользователь период неверно кол-во аргументов
 * и дату, которую невозможно спаристь, протестированы в классе {@link MockitoAverageReportHandlerTest}
 */
public class AverageReportHandlerTest {
    /**
     * Команда
     */
    private final static String COMMAND = "avg_report";

    /**
     * Дата для операций
     */
    private final LocalDate date = LocalDate.of(2023, 12, 1);

    /**
     * Репозиторий для работы с {@link Category}
     */
    private final CategoryRepository categoryRepository;

    /**
     * Репозиторий для работы с {@link Operation}
     */
    private final FakeDatedOperationRepository operationRepository;

    /**
     * Менеджер транзакций
     */
    private final TransactionManager transactionManager;

    /**
     * Класс, позволяющий совершать Hibernate транзакции для очищения хранилищ
     */
    private final ClearQueryManager clearQueryManager;

    /**
     * Обработчик команд телеграм бота
     */
    private final FinanceBotHandler financeBotHandler;

    /**
     * Репозиторий для работы с {@link User}
     */
    private final UserRepository userRepository;

    /**
     * Моковый бот
     */
    private MockBot bot;

    public AverageReportHandlerTest() {
        this.transactionManager = new TransactionManager(new HibernateConfiguration().getSessionFactory());
        this.clearQueryManager = new ClearQueryManager();
        this.operationRepository = new FakeDatedOperationRepository();
        this.categoryRepository = new HibernateCategoryRepository();
        this.userRepository = new HibernateUserRepository();
        this.financeBotHandler = new FinanceBotHandler(
                this.userRepository, this.operationRepository, this.categoryRepository, new HibernateBudgetRepository()
        );
        createDefaultCategories();
    }

    /**
     * Метод создаёт стандартные категории
     */
    public void createDefaultCategories() {
        transactionManager.produceTransaction(session -> {
            try {
                this.categoryRepository.createStandardCategory(session, CategoryType.INCOME, "Зарплата");
                this.categoryRepository.createStandardCategory(session, CategoryType.EXPENSE, "Супермаркеты");
                this.categoryRepository.createStandardCategory(session, CategoryType.EXPENSE, "Рестораны и кафе");
            } catch (ExistingStandardCategoryException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Инициализирует бота перед каждым тестом
     */
    @Before
    public void init() {
        this.bot = new MockBot();
    }

    /**
     * Очищает операции после каждого теста
     */
    @After
    public void clean() {
        transactionManager.produceTransaction(session -> this.clearQueryManager.clear(session, Operation.class, User.class));
    }

    /**
     * Тестирует обработчик на наличие операций от одного пользователя
     */
    @Test
    public void handleCommandWithOneUser() {
        transactionManager.produceTransaction(session -> {
            Category shops = this.categoryRepository.getStandardCategoryByName(session, CategoryType.EXPENSE, "Супермаркеты").get();
            Category restaurant = this.categoryRepository.getStandardCategoryByName(session, CategoryType.EXPENSE, "Рестораны и кафе").get();
            Category salary = this.categoryRepository.getStandardCategoryByName(session, CategoryType.INCOME, "Зарплата").get();
            User user1 = new User(1L, 100000);
            this.userRepository.saveUser(session, user1);

            this.operationRepository.addOperation(session, user1, shops, 200, date);
            this.operationRepository.addOperation(session, user1, shops, 500, date);
            this.operationRepository.addOperation(session, user1, restaurant, 1000, date);
            this.operationRepository.addOperation(session, user1, restaurant, 2000, date);
            this.operationRepository.addOperation(session, user1, salary, 60_000, date);
            this.operationRepository.addOperation(session, user1, salary, 20_000, date);
            CommandData data = new CommandData(this.bot, user1, COMMAND, List.of("12.2023"));

            this.financeBotHandler.handleCommand(data, session);

            String expected = """
                    Подготовил отчет по стандартным категориям со всех пользователей за Декабрь 2023:
                    Зарплата: 80 000 руб.
                    Рестораны и кафе: 3 000 руб.
                    Супермаркеты: 700 руб.
                    """;

            Assert.assertEquals(expected, bot.poolMessageQueue().text());
        });
    }

    /**
     * Тестирует обработчик при условии что операции есть от нескольких пользователей
     */
    @Test
    public void handleCommandWithAnyUsers() {
        transactionManager.produceTransaction(session -> {
            Category shops = this.categoryRepository.getStandardCategoryByName(session, CategoryType.EXPENSE, "Супермаркеты").get();
            Category restaurant = this.categoryRepository.getStandardCategoryByName(session, CategoryType.EXPENSE, "Рестораны и кафе").get();
            Category salary = this.categoryRepository.getStandardCategoryByName(session, CategoryType.INCOME, "Зарплата").get();
            User user1 = new User(1L, 100000);
            User user2 = new User(2L, 100000);
            this.userRepository.saveUser(session, user1);
            this.userRepository.saveUser(session, user2);

            this.operationRepository.addOperation(session, user1, shops, 200);
            this.operationRepository.addOperation(session, user1, shops, 500);
            this.operationRepository.addOperation(session, user1, restaurant, 1000);
            this.operationRepository.addOperation(session, user1, restaurant, 2000);
            this.operationRepository.addOperation(session, user1, salary, 60_000);
            this.operationRepository.addOperation(session, user1, salary, 20_000);

            this.operationRepository.addOperation(session, user2, shops, 500);
            this.operationRepository.addOperation(session, user2, shops, 700);
            this.operationRepository.addOperation(session, user2, restaurant, 400);
            this.operationRepository.addOperation(session, user2, restaurant, 700);
            this.operationRepository.addOperation(session, user2, salary, 40_000);
            this.operationRepository.addOperation(session, user2, salary, 15_000);

            CommandData data = new CommandData(this.bot, user1, COMMAND, List.of("12.2023"));

            this.financeBotHandler.handleCommand(data, session);

            String expected = """
                    Подготовил отчет по стандартным категориям со всех пользователей за Декабрь 2023:
                    Зарплата: 67 500 руб.
                    Рестораны и кафе: 2 050 руб.
                    Супермаркеты: 950 руб.
                    """;

            Assert.assertEquals(expected, bot.poolMessageQueue().text());
        });
    }

    /**
     * Тест без операций на указзанный пользователем период
     */
    @Test
    public void handleCommandIfNoOperationsSpecificDate() {
        transactionManager.produceTransaction(session -> {
            User user = new User(1L, 1);
            this.userRepository.saveUser(session, user);
            CommandData data = new CommandData(this.bot, user, COMMAND, List.of("12.2023"));
            this.financeBotHandler.handleCommand(data, session);
            String expected = "На заданный промежуток данные отсутствуют.";
            Assert.assertEquals(expected, bot.poolMessageQueue().text());
        });
    }

    /**
     * Тест без операций на текущий месяц и год
     */
    @Test
    public void handleCommandIfNoOperationsCurrentDate() {
        transactionManager.produceTransaction(session -> {
            User user = new User(1L, 1);
            this.userRepository.saveUser(session, user);
            CommandData data = new CommandData(this.bot, user, COMMAND, List.of());
            this.financeBotHandler.handleCommand(data, session);
            String expected = "На этот месяц данные отсутствуют.";
            Assert.assertEquals(expected, bot.poolMessageQueue().text());
        });
    }
}
