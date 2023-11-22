package ru.naumen.personalfinancebot.handler;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.services.ReportService;

import java.time.YearMonth;
import java.util.List;


/**
 * Класс для тестирования команды "/report_expense"
 */
public class ReportExpensesHandlerTest {
    /**
     * Фабрика сессии к БД
     */
    private static final SessionFactory sessionFactory;

    /**
     * Репозиторий для работы с операциями
     */
    private static final OperationRepository operationRepository;

    /**
     * Сервис для подготовки отчётов
     */
    private static final ReportService reportService;

    /**
     * Обработчик команды "/report_expense"
     */
    private static final CommandHandler reportExpenseHandler;

    /**
     * Репозиторий для работы с пользователем
     */
    private static final TestHibernateUserRepository userRepository;

    /**
     * Репозиторий для работы с катгеориями
     */
    private static final CategoryRepository categoryRepository;

    static {
        sessionFactory = new HibernateConfiguration().getSessionFactory();
        operationRepository = new HibernateOperationRepository(sessionFactory);
        userRepository = new TestHibernateUserRepository(sessionFactory);
        reportService = new ReportService(operationRepository);
        reportExpenseHandler = new ReportExpensesHandler(reportService);
        categoryRepository = new HibernateCategoryRepository(sessionFactory);
    }

    /**
     * Метод инициализирует пользователя, его категории и операции для этих категорий
     */
    @BeforeClass
    public static void init() throws CategoryRepository.CreatingExistingUserCategoryException, CategoryRepository.CreatingExistingStandardCategoryException {
        User user = new User(1L, 100_000);
        userRepository.saveUser(user);
        Category taxiCategory = categoryRepository.createUserCategory(user, CategoryType.EXPENSE, "Такси");
        Category cleanCategory = categoryRepository.createUserCategory(user, CategoryType.EXPENSE, "Химчистка");
        operationRepository.addOperation(user, taxiCategory, 100);
        operationRepository.addOperation(user, taxiCategory, 200);
        operationRepository.addOperation(user, taxiCategory, 300);
        operationRepository.addOperation(user, taxiCategory, 400);
        operationRepository.addOperation(user, taxiCategory, 500);
        operationRepository.addOperation(user, cleanCategory, 300);
        operationRepository.addOperation(user, cleanCategory, 500);
        operationRepository.addOperation(user, cleanCategory, 700);
        operationRepository.addOperation(user, cleanCategory, 900);
    }

    /**
     * Метод проверяет, что отчёт будет корректно подготовлен, при правильной передаче аргументов
     */
    @Test
    public void handleWithCorrectArguments() {
        User user = userRepository.getUserByTelegramChatId(1L).get();
        MockBot bot = new MockBot();

        YearMonth yearMonth = YearMonth.now();

        List<String> args = List.of("{month}.{year}"
                .replace("{month}", String.valueOf(yearMonth.getMonth().getValue()))
                .replace("{year}", String.valueOf(yearMonth.getYear()))
        );
        HandleCommandEvent commandEvent = new HandleCommandEvent(bot, user, "report_expense", args);

        reportExpenseHandler.handleCommand(commandEvent);

        MockMessage message = bot.poolMessageQueue();

        Assert.assertEquals(
                "Подготовил отчёт по вашим расходам за указанный месяц:\nТакси: 1500.0 руб.\nХимчистка: 2400.0 руб.\n",
                message.text()
        );
    }

    /**
     * Метод проверяет, что будет выведена ошибка в текстовом виде, если не получится спарсить год и месяц
     */
    @Test
    public void handleWithIncorrectArguments() {
        User user = userRepository.getUserByTelegramChatId(1L).get();
        MockBot bot = new MockBot();
        List<String> args = List.of("11.pp", "pp.2023", "pp.pp", "-11.-2023", "-11.2023", "11.-2023");
        for (String arg : args) {
            HandleCommandEvent commandEvent = new HandleCommandEvent(
                    bot, user, "report_expense", List.of(arg)
            );
            reportExpenseHandler.handleCommand(commandEvent);
            MockMessage message = bot.poolMessageQueue();
            Assert.assertEquals(
                    """
                            Переданы неверные данные месяца и года.
                            Дата должна быть передана в виде "MM.YYYY", например, "11.2023".""",
                    message.text()
            );
        }
    }

    /**
     * Метод проверяет, что будет выведена ошибка, если передано неверно количество аргументов.
     */
    @Test
    public void handleWithIncorrectArgumentCount() {
        User user = userRepository.getUserByTelegramChatId(1L).get();
        MockBot bot = new MockBot();
        List<List<String>> argsList = List.of(
                List.of("11", "2023"),
                List.of("Ноябрь", "2023"),
                List.of("-1", "-1"),
                List.of()
        );
        for (List<String> args : argsList) {
            HandleCommandEvent commandEvent =
                    new HandleCommandEvent(bot, user, "report_expense", args);
            reportExpenseHandler.handleCommand(commandEvent);
            MockMessage message = bot.poolMessageQueue();
            Assert.assertEquals(
                    "Команда /report_expense принимает 1 аргумент [mm.yyyy], например \"/report_expense 11.2023\"",
                    message.text()
            );
        }
    }
}