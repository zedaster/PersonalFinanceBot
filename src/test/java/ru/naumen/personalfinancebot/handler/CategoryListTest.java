package ru.naumen.personalfinancebot.handler;

import org.hibernate.SessionFactory;
import org.junit.*;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.TestHibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.TestHibernateUserRepository;

import java.util.List;

/**
 * Тесты на команды для вывода категорий
 */
public class CategoryListTest {
    /**
     * Session factory для работы с сессиями в хранилищах
     */
    private static final SessionFactory sessionFactory;

    /**
     * Хранилище пользователей
     */
    private final TestHibernateUserRepository userRepository;

    /**
     * Хранилище категорий
     * Данная реализация позволяет сделать полную очистку категорий после тестов
     */
    private final TestHibernateCategoryRepository categoryRepository;

    /**
     * Хранилище операций
     */
    private final OperationRepository operationRepository;

    /**
     * Обработчик команд для бота
     */
    private final FinanceBotHandler botHandler;

    /**
     * Моковый пользователь. Пересоздается для каждого теста
     */
    private final User mockUser;

    /**
     * Моковый бот. Пересоздается для каждого теста.
     */
    private MockBot mockBot;

    // Инициализация статических полей перед использованием класса
    static {
        sessionFactory = new HibernateConfiguration().getSessionFactory();
    }

    public CategoryListTest() {
        this.userRepository = new TestHibernateUserRepository(sessionFactory);
        this.categoryRepository = new TestHibernateCategoryRepository(sessionFactory);
        this.operationRepository = new HibernateOperationRepository(sessionFactory);
        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository);

        this.mockUser = new User(1L, 100);
        this.userRepository.saveUser(this.mockUser);
    }

    /**
     * Очистка стандартных категорий и закрытие sessionFactory после выполнения всех тестов в этом классе
     */
    @AfterClass
    public static void finishTests() {
        sessionFactory.close();
    }

    /**
     * Создаем пользователя и бота перед каждым тестом
     */
    @Before
    public void beforeEachTest() {
        this.mockBot = new MockBot();

        // Наполняем стандартные категории перед тестами
        try {
            categoryRepository.createStandardCategory(CategoryType.INCOME, "Standard income 1");
            categoryRepository.createStandardCategory(CategoryType.INCOME, "Standard income 2");
            categoryRepository.createStandardCategory(CategoryType.EXPENSE, "Standard expense 1");
            categoryRepository.createStandardCategory(CategoryType.EXPENSE, "Standard expense 2");
        } catch (CategoryRepository.CreatingExistingCategoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Удаляем всех пользователей из БД после каждого теста
     */
    @After
    public void afterEachTest() {
        categoryRepository.removeAll();
        userRepository.removeAll();
    }

    /**
     * Тестирует отображение доходов+расходов или доходов или расходов нескольких (трех) категорий доходов и
     * нескольких (трех) категорий расходов.
     */
    @Test
    public void showCoupleOfCategories() throws CategoryRepository.CreatingExistingCategoryException {
        final String expectFullMsg = """
                Все доступные вам категории доходов:
                Стандартные:
                1. Standard income 1
                2. Standard income 2
                                
                Персональные:
                1. Personal income 1
                2. Personal income 2
                3. Personal income 3
                                
                Все доступные вам категории расходов:
                Стандартные:
                1. Standard expense 1
                2. Standard expense 2
                                
                Персональные:
                1. Personal expense 1
                2. Personal expense 2
                3. Personal expense 3
                """;

        final String expectIncomeMsg = """
                Все доступные вам категории доходов:
                Стандартные:
                1. Standard income 1
                2. Standard income 2
                                
                Персональные:
                1. Personal income 1
                2. Personal income 2
                3. Personal income 3
                """;

        final String expectExpensesMsg = """
                Все доступные вам категории расходов:
                Стандартные:
                1. Standard expense 1
                2. Standard expense 2
                                
                Персональные:
                1. Personal expense 1
                2. Personal expense 2
                3. Personal expense 3
                """;

        addUserCategories(this.mockUser, CategoryType.INCOME, "Personal income 1", "Personal income 2",
                "Personal income 3");
        addUserCategories(this.mockUser, CategoryType.EXPENSE, "Personal expense 1", "Personal expense 2",
                "Personal expense 3");

        final List<String> commands = List.of("list_categories", "list_income_categories", "list_expense_categories");
        for (String commandName : commands) {
            HandleCommandEvent allCommand = new HandleCommandEvent(
                    this.mockBot,
                    this.mockUser,
                    commandName,
                    List.of());
            this.botHandler.handleCommand(allCommand);
        }

        Assert.assertEquals(3, this.mockBot.getMessageQueueSize());
        Assert.assertEquals(expectFullMsg, this.mockBot.poolMessageQueue().text());
        Assert.assertEquals(expectIncomeMsg, this.mockBot.poolMessageQueue().text());
        Assert.assertEquals(expectExpensesMsg, this.mockBot.poolMessageQueue().text());
    }

    /**
     * Тестирует отображение по 1 категории на доход и расход у пользователя
     */
    @Test
    public void showOneCategory() throws CategoryRepository.CreatingExistingCategoryException {
        final String expectMsg = """
                Все доступные вам категории доходов:
                Стандартные:
                1. Standard income 1
                2. Standard income 2
                                
                Персональные:
                1. Personal income 1
                                
                Все доступные вам категории расходов:
                Стандартные:
                1. Standard expense 1
                2. Standard expense 2
                                
                Персональные:
                1. Personal expense 1
                """;

        addUserCategories(this.mockUser, CategoryType.INCOME, "Personal income 1");
        addUserCategories(this.mockUser, CategoryType.EXPENSE, "Personal expense 1");

        HandleCommandEvent command = new HandleCommandEvent(this.mockBot, this.mockUser, "list_categories",
                List.of());
        this.botHandler.handleCommand(command);
        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        MockMessage lastMessage = this.mockBot.poolMessageQueue();
        Assert.assertEquals(expectMsg, lastMessage.text());
    }

    /**
     * Тестирует отображение пользовательских категорий при их отсутствии
     */
    @Test
    public void showNoCategories() {
        final String expectMsg = """
                Все доступные вам категории доходов:
                Стандартные:
                1. Standard income 1
                2. Standard income 2
                                
                Персональные:
                <отсутствуют>
                                
                Все доступные вам категории расходов:
                Стандартные:
                1. Standard expense 1
                2. Standard expense 2
                                
                Персональные:
                <отсутствуют>
                """;

        HandleCommandEvent command = new HandleCommandEvent(this.mockBot, this.mockUser, "list_categories",
                List.of());
        this.botHandler.handleCommand(command);
        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        MockMessage lastMessage = this.mockBot.poolMessageQueue();
        Assert.assertEquals(expectMsg, lastMessage.text());
    }

    /**
     * Проверяет, отобразятся ли категории одного пользователя у другого
     */
    @Test
    public void privacyOfPersonalCategories() throws CategoryRepository.CreatingExistingCategoryException {
        User secondUser = new User(2L, 200.0);
        userRepository.saveUser(secondUser);

        addUserCategories(this.mockUser, CategoryType.INCOME, "Personal income 1", "Personal income 2",
                "Personal income 3");

        final String expectMockUserMsg = """
                Все доступные вам категории доходов:
                Стандартные:
                1. Standard income 1
                2. Standard income 2
                                
                Персональные:
                1. Personal income 1
                2. Personal income 2
                3. Personal income 3
                                
                Все доступные вам категории расходов:
                Стандартные:
                1. Standard expense 1
                2. Standard expense 2
                                
                Персональные:
                <отсутствуют>
                """;

        final String expectSecondUserMsg = """
                Все доступные вам категории доходов:
                Стандартные:
                1. Standard income 1
                2. Standard income 2
                                
                Персональные:
                <отсутствуют>
                                
                Все доступные вам категории расходов:
                Стандартные:
                1. Standard expense 1
                2. Standard expense 2
                                
                Персональные:
                <отсутствуют>
                """;

        List<User> users = List.of(this.mockUser, secondUser);
        List<String> expectMessages = List.of(expectMockUserMsg, expectSecondUserMsg);
        for (int i = 0; i < 2; i++) {
            HandleCommandEvent command = new HandleCommandEvent(
                    this.mockBot,
                    users.get(i),
                    "list_categories",
                    List.of());
            this.botHandler.handleCommand(command);

            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage lastMessage = this.mockBot.poolMessageQueue();
            Assert.assertEquals(users.get(i), lastMessage.receiver());
            Assert.assertEquals(expectMessages.get(i), lastMessage.text());
        }
    }

    private void addUserCategories(User user, CategoryType type, String... names) throws
            CategoryRepository.CreatingExistingUserCategoryException,
            CategoryRepository.CreatingExistingStandardCategoryException {
        for (String name : names) {
            this.categoryRepository.createUserCategory(user, type, name);
        }
    }
}
