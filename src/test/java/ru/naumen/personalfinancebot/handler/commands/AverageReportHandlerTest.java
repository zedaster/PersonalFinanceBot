package ru.naumen.personalfinancebot.handler.commands;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;
import ru.naumen.personalfinancebot.services.ArgumentParseService;
import ru.naumen.personalfinancebot.services.OutputFormatService;
import ru.naumen.personalfinancebot.services.ReportService;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для тестирования класса {@link AverageReportHandler}
 */
public class AverageReportHandlerTest {
    private final static String COMMAND_NAME = "avg_report";

    /**
     * Фабрика сессий
     */
    private SessionFactory sessionFactory;
    /**
     * Репозиторий для работы с операциями ({@link Operation})
     */
    private OperationRepository operationRepository;

    /**
     * Репозиторий для работы с пользователями {@link User}
     */
    private UserRepository userRepository;

    /**
     * Сервис для парсинга полученных аргументов
     */
    private ArgumentParseService argumentParseService;

    /**
     * Сервис для подготовки отчётов
     */
    private ReportService reportService;

    /**
     * Обработчик команды "/avg_report" - Тестируемый класс
     */
    private AverageReportHandler averageReportHandler;

    /**
     * Репозиторий для работы с Категориями ({@link Category})
     */
    private CategoryRepository categoryRepository;

    /**
     * Сервис форматирования
     */
    private OutputFormatService outputFormatService;

    @Before
    public void initializeFields() {
        this.outputFormatService = new OutputFormatService();
        this.sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.operationRepository = new HibernateOperationRepository(this.sessionFactory);
        this.argumentParseService = new ArgumentParseService();
        this.reportService = new ReportService(this.operationRepository, outputFormatService);
        this.averageReportHandler = new AverageReportHandler(this.argumentParseService, this.reportService);
        this.userRepository = new HibernateUserRepository(this.sessionFactory);
        this.categoryRepository = new HibernateCategoryRepository(this.sessionFactory);

        User user = new User(1L, 100_00);
        this.userRepository.saveUser(user);
    }

    /**
     * Метод тестирует обработчик на неправильно переданное количество аргументов
     * Обработчик принимать 0 или 1 аргумент.
     */
    @Test
    public void testIncorrectArgumentsCount() {
        User user = this.userRepository.getUserByTelegramChatId(1L).get();
        MockBot bot = new MockBot();
        List<List<String>> argumentsList = List.of(
                List.of("аргумент1", "аргумент 2"),
                List.of("аргумент1", "аргумент 2", "аргумент 3"),
                List.of("аргумент1", "аргумент 2", "аргуменt 3", "аргумент 4")
        );

        for (List<String> arguments : argumentsList) {
            HandleCommandEvent commandEvent = new HandleCommandEvent(bot, user, COMMAND_NAME, arguments);
            this.averageReportHandler.handleCommand(commandEvent);
            MockMessage message = bot.poolMessageQueue();
            Assert.assertEquals(
                    "Команда \"/avg_report\" не принимает аргументов,"
                            + " либо принимает Месяц и Год в формате \"MM.YYYY\"."
                            + "\nНапример, \"/avg_report\" или \"/avg_report 12.2023\"."
                    , message.text()
            );
        }
    }

    /**
     * Метод тестирует обработчик на правильный парсинг переданной даты [MM.YYYY].
     * В данной тесте передаётся неверная даты [MM.YYYY]
     */
    @Test
    public void testIncorrectGivenYearMonth() {
        List<List<String>> incorrectYearMonths = List.of(
                List.of("pp.1111"), List.of("1111"), List.of("12.pppp"),
                List.of("-12.-2023"), List.of("122023"), List.of("Декабрь 2023"),
                List.of("ASDFWASD")
        );
        User user = this.userRepository.getUserByTelegramChatId(1L).get();
        MockBot bot = new MockBot();

        for (List<String> arguments : incorrectYearMonths) {
            HandleCommandEvent commandEvent = new HandleCommandEvent(bot, user, COMMAND_NAME, arguments);
            this.averageReportHandler.handleCommand(commandEvent);

            MockMessage message = bot.poolMessageQueue();

            Assert.assertEquals(user.getId(), message.receiver().getId());
            Assert.assertEquals(
                    "Дата введена неверно! Введите ее в формате [mm.yyyy - месяц.год]",
                    message.text()
            );
        }
    }

    /**
     * Метод проверяет, что вместо пустого отчета будет выведено сообщение об отсутсвии данных.
     */
    @Test
    public void testEmptyReport() {
        List<String> argument = List.of("12.2023");
        User user = this.userRepository.getUserByTelegramChatId(1L).get();
        MockBot bot = new MockBot();
        HandleCommandEvent commandEvent = new HandleCommandEvent(bot, user, COMMAND_NAME, argument);
        this.averageReportHandler.handleCommand(commandEvent);
        MockMessage message = bot.poolMessageQueue();
        Assert.assertEquals(user.getId(), message.receiver().getId());
        Assert.assertEquals("На данный момент данные отсутсвуют. Попробуйте позже...", message.text());
    }

    /**
     * Создает пользователей и возвращает список
     *
     * @return Список пользователей
     */
    private List<User> createAndGetUsers() {
        List<User> users = new ArrayList<>();
        users.add(this.userRepository.getUserByTelegramChatId(1L).get());
        for (int i = 2; i < 5; i++) {
            User user = new User(i, 100_000);
            this.userRepository.saveUser(user);
            users.add(user);
        }
        return users;
    }

    /**
     * Создает стандартные категории
     *
     * @return Возвращает список созданных категорий
     * @throws CategoryRepository.CreatingExistingStandardCategoryException
     */
    private List<Category> createDefaultCategories() throws CategoryRepository.CreatingExistingStandardCategoryException {
        return List.of(
                this.categoryRepository.createStandardCategory(CategoryType.EXPENSE, "Супермаркеты"),
                this.categoryRepository.createStandardCategory(CategoryType.EXPENSE, "Аптеки"),
                this.categoryRepository.createStandardCategory(CategoryType.EXPENSE, "Развлечения")
        );
    }

    /**
     * Создаёт операцию пользователя для каждой категории
     *
     * @param users      Список пользователей
     * @param categories Список категорий
     */
    private void createOperations(List<User> users, List<Category> categories) {
        for (Category category : categories) {
            for (User user : users) {
                this.operationRepository.addOperation(user, category, 450);
            }
        }
    }

    /**
     * Тест на кореектную подготовку отчётов
     */
    @Test
    public void correctReportTest() throws CategoryRepository.CreatingExistingStandardCategoryException {
        createOperations(createAndGetUsers(), createDefaultCategories());
        MockBot bot = new MockBot();
        HandleCommandEvent commandEvent = new HandleCommandEvent(
                bot, this.userRepository.getUserByTelegramChatId(1L).get(), "avg_report", List.of());

        this.averageReportHandler.handleCommand(commandEvent);
        MockMessage message = bot.poolMessageQueue();
        YearMonth ym = YearMonth.now();
        String month = this.outputFormatService.formatRuMonthName(ym.getMonth());

        String expected = """
                Подготовил отчет по стандартным категориям со всех пользователей за {ruMonth} {year}:
                Аптеки: 450 руб.
                Развлечения: 450 руб.
                Супермаркеты: 450 руб.
                """
                .replace("{ruMonth}", month)
                .replace("{year}", String.valueOf(ym.getYear()));

        Assert.assertEquals(expected, message.text());
    }
}