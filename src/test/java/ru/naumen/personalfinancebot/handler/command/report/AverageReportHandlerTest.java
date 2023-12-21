package ru.naumen.personalfinancebot.handler.command.report;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
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

    /**
     * Стандартная Категория доходов "Зарлпта"
     */
    private Category salaryCategory;

    /**
     * Стандартная Категория расходов "Супермаркеты"
     */
    private Category shopsCategory;

    /**
     * Стандартная Категория расходов "Рестораны и кафе"
     */
    private Category restaurantCategory;

    public AverageReportHandlerTest() {
        this.transactionManager = new TransactionManager(new HibernateConfiguration().getSessionFactory());
        this.clearQueryManager = new ClearQueryManager();
        this.operationRepository = new FakeDatedOperationRepository();
        this.categoryRepository = new HibernateCategoryRepository();
        this.userRepository = new HibernateUserRepository();
        this.financeBotHandler = new FinanceBotHandler(
                this.userRepository, this.operationRepository, this.categoryRepository, new HibernateBudgetRepository()
        );
    }

    /**
     * Инициализирует бота перед каждым тестом и создает стандартные категории перед каждый тестом
     */
    @Before
    public void init() {
        this.bot = new MockBot();
        transactionManager.produceTransaction(session -> {
            try {
                this.salaryCategory = this.categoryRepository.createStandardCategory(
                        session, CategoryType.INCOME, "Зарплата");
                this.shopsCategory = this.categoryRepository.createStandardCategory(
                        session, CategoryType.EXPENSE, "Супермаркеты");
                this.restaurantCategory = this.categoryRepository.createStandardCategory(
                        session, CategoryType.EXPENSE, "Рестораны и кафе");
            } catch (ExistingStandardCategoryException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Очищает таблицами с операциями, категориями и пользователями после каждого теста
     */
    @After
    public void clean() {
        transactionManager.produceTransaction(session -> this.clearQueryManager.clear(
                session, Operation.class, User.class, Category.class));
    }

    /**
     * Тестирует обработчик на наличие операций от одного пользователя
     */
    @Test
    public void handleCommandWithOneUser() {
        transactionManager.produceTransaction(session -> {
            User user1 = new User(1L, 100000);
            this.userRepository.saveUser(session, user1);

            this.operationRepository.addOperation(session, user1, shopsCategory, 200, date);
            this.operationRepository.addOperation(session, user1, shopsCategory, 500, date);
            this.operationRepository.addOperation(session, user1, restaurantCategory, 1000, date);
            this.operationRepository.addOperation(session, user1, restaurantCategory, 2000, date);
            this.operationRepository.addOperation(session, user1, salaryCategory, 60_000, date);
            this.operationRepository.addOperation(session, user1, salaryCategory, 20_000, date);
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
            User user1 = new User(1L, 100000);
            User user2 = new User(2L, 100000);
            this.userRepository.saveUser(session, user1);
            this.userRepository.saveUser(session, user2);

            this.operationRepository.addOperation(session, user1, shopsCategory, 200);
            this.operationRepository.addOperation(session, user1, shopsCategory, 500);
            this.operationRepository.addOperation(session, user1, restaurantCategory, 1000);
            this.operationRepository.addOperation(session, user1, restaurantCategory, 2000);
            this.operationRepository.addOperation(session, user1, salaryCategory, 60_000);
            this.operationRepository.addOperation(session, user1, salaryCategory, 20_000);

            this.operationRepository.addOperation(session, user2, shopsCategory, 500);
            this.operationRepository.addOperation(session, user2, shopsCategory, 700);
            this.operationRepository.addOperation(session, user2, restaurantCategory, 400);
            this.operationRepository.addOperation(session, user2, restaurantCategory, 700);
            this.operationRepository.addOperation(session, user2, salaryCategory, 40_000);
            this.operationRepository.addOperation(session, user2, salaryCategory, 15_000);

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

    /**
     * Тест на наверно переданное кол-во аргументов
     */
    @Test
    public void handleCommandIfIncorrectArgsCount() {
        User user = new User(1L, 1);
        String expected = """
                Команда "/avg_report" не принимает аргументы, либо принимает Месяц и Год в формате "MM.YYYY"
                Например, "/avg_report" или "/avg_report 12.2023".""";

        List<List<String>> argsList = List.of(
                List.of("12.2023", "12.2023"),
                List.of("12", "2023"),
                List.of("p", "pppp", "12.2023"),
                List.of("", "", "")
        );

        for (List<String> args : argsList) {
            CommandData data = new CommandData(this.bot, user, COMMAND, args);
            this.financeBotHandler.handleCommand(data, null);
            Assert.assertEquals(expected, this.bot.poolMessageQueue().text());
        }
    }

    /**
     * Тест на неверно переданную дату
     */
    @Test
    public void handleCommandIfIncorrectDateFormat() {
        List<List<String>> argsList = List.of(
                List.of("pp.2023"), List.of("12.pppp"),
                List.of("p"), List.of(""),
                List.of("Декабрь 2023"), List.of("1985‑09‑25 17:45:30.005")
        );
        User user = new User(1, 1);
        String expected = "Дата введена неверно! Введите ее в формате [mm.yyyy - месяц.год]";
        for (List<String> args : argsList) {
            CommandData data = new CommandData(this.bot, user, COMMAND, args);
            this.financeBotHandler.handleCommand(data, null);

            MockMessage message = this.bot.poolMessageQueue();
            Assert.assertEquals(expected, message.text());
        }
    }
}
