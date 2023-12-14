package ru.naumen.personalfinancebot.handler.command.report;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
import ru.naumen.personalfinancebot.repository.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingStandardCategoryException;
import ru.naumen.personalfinancebot.repository.operation.FakeDatedOperationRepository;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.service.InputDateFormatService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Тесты для команды "/estimate_report"
 */
public class EstimateReportTest {
    /**
     * Название команды
     */
    private static final String COMMAND_NAME = "estimate_report";

    /**
     * Репозиторий, работающий с пользователями
     */
    private final HibernateUserRepository userRepository;

    /**
     * Репозиторий, работающий с операциями
     */
    private final FakeDatedOperationRepository operationRepository;

    /**
     * Репозиторий, работающий с категориями
     */
    private final HibernateCategoryRepository categoryRepository;

    /**
     * Обработчик команд в боте
     */
    private final FinanceBotHandler botHandler;

    /**
     * Менеджер для откртия транзакций
     */
    private final TransactionManager transactionManager;

    /**
     * Сервис, который форматирует дату для подачи ее в аргументы команды
     */
    private final InputDateFormatService inputDateFormatService;

    /**
     * Моковый бот
     */
    private MockBot mockBot;

    /**
     * Тестовая стандартная категория дохода
     */
    private Category testStandartIncomeCategory;

    /**
     * Тестовая стандартная категория расхода
     */
    private Category testStandartExpsenseCategory;

    /**
     * Тестовый пользователь
     */
    private User testUser;

    public EstimateReportTest() {
        this.userRepository = new HibernateUserRepository();
        this.operationRepository = new FakeDatedOperationRepository();
        this.categoryRepository = new HibernateCategoryRepository();
        this.botHandler = new FinanceBotHandler(
                this.userRepository,
                this.operationRepository,
                this.categoryRepository,
                null);
        SessionFactory sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.transactionManager = new TransactionManager(sessionFactory);
        this.inputDateFormatService = new InputDateFormatService();
    }

    /**
     * Создание бота, первого пользователя и категорий перед каждым тестом
     */
    @Before
    public void beforeEach() {
        this.mockBot = new MockBot();
        transactionManager.produceTransaction(session -> {
            try {
                this.testStandartIncomeCategory = this.categoryRepository
                        .createStandardCategory(session, CategoryType.INCOME, "Test income");
                this.testStandartExpsenseCategory = this.categoryRepository
                        .createStandardCategory(session, CategoryType.EXPENSE, "Test expense");
            } catch (ExistingStandardCategoryException e) {
                throw new RuntimeException(e);
            }
            this.testUser = createUser(session, 1);
        });

    }

    /**
     * Очищение репозиториев после каждого теста
     */
    @After
    public void afterEach() {
        transactionManager.produceTransaction(session -> {
            new ClearQueryManager().clear(session, Operation.class, Category.class, User.class);
        });
    }

    /**
     * Тестирует команду без аргумента (в текущем месяце) с операциями в БД
     */
    @Test
    public void currentMonth() {
        final String expected = """
                Подготовил отчет по средним доходам и расходам пользователей за текущий месяц:
                Расходы: 60 000
                Доходы: 90 000""";

        transactionManager.produceTransaction(session -> {
            this.operationRepository.addOperation(session, this.testUser, this.testStandartExpsenseCategory, 20_000);
            this.operationRepository.addOperation(session, this.testUser, this.testStandartExpsenseCategory, 30_000);
            this.operationRepository.addOperation(session, this.testUser, this.testStandartIncomeCategory, 80_000);

            User secondUser = createUser(session, 2);
            this.operationRepository.addOperation(session, secondUser, this.testStandartExpsenseCategory, 70_000);
            this.operationRepository.addOperation(session, secondUser, this.testStandartIncomeCategory, 100_000);

            CommandData commandData = new CommandData(this.mockBot, this.testUser, COMMAND_NAME, List.of());
            botHandler.handleCommand(commandData, session);
        });

        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        Assert.assertEquals(expected, this.mockBot.poolMessageQueue().text());
    }

    /**
     * Тестирует команду без аргумента (в текущем месяце) без операций в БД
     */
    @Test
    public void currentMonthNoData() {
        final String expected = "На этот месяц данные отсутствуют.";

        transactionManager.produceTransaction(session -> {
            CommandData commandData = new CommandData(this.mockBot, this.testUser, COMMAND_NAME, List.of());
            botHandler.handleCommand(commandData, session);
        });

        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        Assert.assertEquals(expected, this.mockBot.poolMessageQueue().text());
    }

    /**
     * Тестирует команду в будущем периоде
     */
    @Test
    public void futureMonth() {
        final String expected = "На заданный промежуток данные отсутствуют.";

        transactionManager.produceTransaction(session -> {
            YearMonth futureYearMonth = YearMonth.now().plusMonths(1);
            List<String> args = List.of(inputDateFormatService.formatYearMonth(futureYearMonth));
            CommandData commandData = new CommandData(this.mockBot, this.testUser, COMMAND_NAME, args);
            botHandler.handleCommand(commandData, session);
        });

        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        Assert.assertEquals(expected, this.mockBot.poolMessageQueue().text());
    }

    /**
     * Тестирует команду в периоде в прошлом
     */
    @Test
    public void pastMonth() {
        final String expected = """
                Подготовил отчет по средним доходам и расходам пользователей за Октябрь 2023:
                Расходы: 70 000
                Доходы: 100 000""";
        final LocalDate createDate = YearMonth.of(2023, 10).atDay(1);

        transactionManager.produceTransaction(session -> {
            this.operationRepository.addOperation(session, this.testUser, this.testStandartExpsenseCategory,
                    80_000, createDate);
            this.operationRepository.addOperation(session, this.testUser, this.testStandartIncomeCategory,
                    90_000, createDate);

            User secondUser = createUser(session, 2);
            this.operationRepository.addOperation(session, secondUser, this.testStandartExpsenseCategory,
                    60_000, createDate);
            this.operationRepository.addOperation(session, secondUser, this.testStandartIncomeCategory,
                    110_000, createDate);

            CommandData commandData = new CommandData(this.mockBot, this.testUser, COMMAND_NAME,
                    List.of("10.2023"));
            botHandler.handleCommand(commandData, session);
        });

        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        Assert.assertEquals(expected, this.mockBot.poolMessageQueue().text());

    }

    /**
     * Тестирует команду при наличии данных только на один тип категории
     */
    @Test
    public void onlyOneCategory() {
        final String expected = """
                Подготовил отчет по средним доходам и расходам пользователей за текущий месяц:
                Расходы: <отсутствуют>
                Доходы: 50 000""";

        transactionManager.produceTransaction(session -> {
            this.operationRepository.addOperation(session, this.testUser,
                    this.testStandartIncomeCategory, 50_000);

            CommandData commandData = new CommandData(this.mockBot, this.testUser, COMMAND_NAME, List.of());
            botHandler.handleCommand(commandData, session);
        });

        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        Assert.assertEquals(expected, this.mockBot.poolMessageQueue().text());
    }

    /**
     * Тестирует аргуметы, количество которых больше необходимого
     */
    @Test
    public void wrongDate() {
        final String expected = "Дата введена неверно! Введите ее в формате [mm.yyyy - месяц.год]";

        List<List<String>> wrongCases = List.of(
                List.of("kkk")
        );

        transactionManager.produceTransaction(session -> {
            for (List<String> args : wrongCases) {
                CommandData commandData = new CommandData(this.mockBot, this.testUser, COMMAND_NAME, args);
                botHandler.handleCommand(commandData, session);
                Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
                Assert.assertEquals(expected, this.mockBot.poolMessageQueue().text());
            }
        });

    }

    /**
     * Тестирует аргуметы, количество которых больше необходимого
     */
    @Test
    public void wrongArgCount() {
        final String expected = """
                Команда "/estimate_report" не принимает аргументов, либо принимает Месяц и Год в формате "MM.YYYY".
                Например, "/estimate_report" или "/estimate_report 12.2023".""";

        String formattedCurrentYearMonth = inputDateFormatService.formatYearMonth(YearMonth.now());
        List<List<String>> wrongCases = List.of(
                List.of(formattedCurrentYearMonth, formattedCurrentYearMonth),
                List.of(formattedCurrentYearMonth, "a"),
                List.of(formattedCurrentYearMonth, "1")
        );

        transactionManager.produceTransaction(session -> {
            for (List<String> args : wrongCases) {
                CommandData commandData = new CommandData(this.mockBot, this.testUser, COMMAND_NAME, args);
                botHandler.handleCommand(commandData, session);
                Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
                Assert.assertEquals(expected, this.mockBot.poolMessageQueue().text());
            }
        });
    }

    /**
     * Создает и сохраняет пользователя для тестов
     *
     * @param session Сессия
     * @param id      идентификатор
     * @return Новый пользователь
     */
    private User createUser(Session session, long id) {
        User user = new User(id, 0);
        this.userRepository.saveUser(session, user);
        return user;
    }
}
