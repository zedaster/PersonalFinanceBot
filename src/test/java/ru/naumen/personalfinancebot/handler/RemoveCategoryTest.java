package ru.naumen.personalfinancebot.handler;

import org.hibernate.SessionFactory;
import org.junit.*;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.TestHibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.TestHibernateUserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Класс для тестирования удаления пользовательских категорий
 */
public class RemoveCategoryTest {
    /**
     * Session factory для работы с сессиями в хранилищах
     */
    private static final SessionFactory sessionFactory;

    /**
     * Команда для удаления категории дохода
     */
    private static final String REMOVE_INCOME_COMMAND = "remove_income_category";

    /**
     * Команда для удаления категории расхода
     */
    private static final String REMOVE_EXPENSE_COMMAND = "remove_expense_category";

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
     * Обработчик команд
     */
    private final FinanceBotHandler botHandler;

    /**
     * Моковый пользователь. Пересоздается перед каждым тестом
     */
    private User mockUser;

    /**
     * Моковый бот. Пересоздается перед каждым тестом
     */
    private MockBot mockBot;


    // Инициализация статических полей перед использованием класса
    static {
        sessionFactory = new HibernateConfiguration().getSessionFactory();
    }

    public RemoveCategoryTest() {
        userRepository = new TestHibernateUserRepository(sessionFactory);
        categoryRepository = new TestHibernateCategoryRepository(sessionFactory);
        operationRepository = new HibernateOperationRepository(sessionFactory);
        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository);
    }

    /**
     * Очистка стандартных значений и закрытие sessionFactory после выполнения всех тестов в этом классе
     */
    @AfterClass
    public static void finishTests() {
        sessionFactory.close();
    }

    /**
     * Создаем пользователя и бота перед каждым тестом
     * У пользователя будут категории Personal Income 1 и Personal Expense 1
     */
    @Before
    public void beforeEachTest() {
        this.mockUser = createTestUser(1);
        this.mockBot = new MockBot();
    }

    /**
     * Удаляем пользователя из БД и его категории после каждого теста
     */
    @After
    public void afterEachTest() {
        categoryRepository.removeAll();
        userRepository.removeAll();
    }

    /**
     * Тестирует удаление одной из существующих категорий расходов.
     */
    @Test
    public void removeOneOfOneExistingExpenseCategory() throws CategoryRepository.CreatingExistingUserCategoryException,
            CategoryRepository.CreatingExistingStandardCategoryException {
        final List<String> categoryNames = List.of(
                "Жкх",
                "Жилищно-коммунальные услуги",
                "Покупки"
        );
        final List<List<String>> argsList = List.of(
                List.of("Жкх"),
                List.of("Жилищно-коммунальные", "услуги"),
                List.of("ПоКуПкИ"));
        final List<String> responses = List.of(
                "Категория расходов 'Жкх' успешно удалена",
                "Категория расходов 'Жилищно-коммунальные услуги' успешно удалена",
                "Категория расходов 'Покупки' успешно удалена"
        );

        for (int i = 0; i < categoryNames.size(); i++) {
            String categoryName = categoryNames.get(i);
            List<String> args = argsList.get(i);
            categoryRepository.createUserCategory(this.mockUser, CategoryType.EXPENSE, categoryName);
            HandleCommandEvent command = new HandleCommandEvent(this.mockBot, this.mockUser, REMOVE_EXPENSE_COMMAND,
                    args);
            this.botHandler.handleCommand(command);

            Optional<Category> category = categoryRepository.getCategoryByName(this.mockUser, CategoryType.EXPENSE,
                    categoryName);
            Assert.assertTrue(category.isEmpty());
        }

        Assert.assertEquals(responses.size(), this.mockBot.getMessageQueueSize());
        for (String response : responses) {
            Assert.assertEquals(response, this.mockBot.poolMessageQueue().text());
        }
    }

    /**
     * Тестирует удаление одной из существующих категорий дохода и расхода с тем же названием.
     */
    @Test
    public void removeOneNameDifferentType() throws CategoryRepository.CreatingExistingCategoryException {
        final String categoryName = "ЖКХ";
        final String response = "Категория расходов 'Жкх' успешно удалена";

        categoryRepository.createUserCategory(this.mockUser, CategoryType.INCOME, categoryName);
        categoryRepository.createUserCategory(this.mockUser, CategoryType.EXPENSE, categoryName);

        HandleCommandEvent command = new HandleCommandEvent(this.mockBot, this.mockUser, REMOVE_EXPENSE_COMMAND,
                List.of(categoryName));
        this.botHandler.handleCommand(command);

        // Несуществование в базе удаленного элемента уже проверено выше

        Assert.assertTrue(categoryRepository
                .getCategoryByName(this.mockUser, CategoryType.INCOME, categoryName)
                .isPresent());

        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        Assert.assertEquals(response, this.mockBot.poolMessageQueue().text());
    }

    /**
     * Тестирует невозможность удаления несуществующих категорий.
     */
    @Test
    public void removeNonExistingCategory() {
        HandleCommandEvent command = new HandleCommandEvent(this.mockBot, this.mockUser, REMOVE_EXPENSE_COMMAND,
                List.of("No", "name"));
        this.botHandler.handleCommand(command);

        Assert.assertEquals(this.mockBot.getMessageQueueSize(), 1);
        MockMessage lastMessage = this.mockBot.poolMessageQueue();
        Assert.assertEquals("Пользовательской категории расходов 'No name' не существует!", lastMessage.text());
    }

    /**
     * Тестирует удаление категории у одного пользователя, когда у еще одного пользователя есть категория с тем же
     * названием и типом.
     */
    @Test
    public void removeExistingCategoryWithTwoUsers() throws CategoryRepository.CreatingExistingCategoryException {
        final String testSameIncomeCategoryName = "Super-income";

        User secondUser = createTestUser(2);
        categoryRepository.createUserCategory(this.mockUser, CategoryType.INCOME,
                testSameIncomeCategoryName);
        categoryRepository.createUserCategory(secondUser, CategoryType.INCOME,
                testSameIncomeCategoryName);

        HandleCommandEvent command = new HandleCommandEvent(this.mockBot, this.mockUser, REMOVE_INCOME_COMMAND,
                List.of(testSameIncomeCategoryName));
        this.botHandler.handleCommand(command);

        Assert.assertTrue(categoryRepository
                .getCategoryByName(secondUser, CategoryType.INCOME, testSameIncomeCategoryName)
                .isPresent());
    }

    /**
     * Тестирует невозможность удаления стандартной категории пользователем.
     */
    @Test
    public void removeStandardCategoryByUser() throws CategoryRepository.CreatingExistingStandardCategoryException {
        final String categoryName = "Standard";
        final String expectedMessage = "Пользовательской категории расходов 'Standard' не существует!";

        categoryRepository.createStandardCategory(CategoryType.EXPENSE, categoryName);

        HandleCommandEvent command = new HandleCommandEvent(this.mockBot, this.mockUser, REMOVE_EXPENSE_COMMAND,
                List.of(categoryName));
        this.botHandler.handleCommand(command);

        Assert.assertTrue(categoryRepository
                .getCategoryByName(this.mockUser, CategoryType.EXPENSE, categoryName)
                .isPresent());

        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        Assert.assertEquals(expectedMessage, this.mockBot.poolMessageQueue().text());
    }

    /**
     * Тестирует невозможность удаления категории, если аргументы введены неверно
     */
    @Test
    public void removeWrongArgs() throws CategoryRepository.CreatingExistingUserCategoryException,
            CategoryRepository.CreatingExistingStandardCategoryException {
        final String categoryName = "Жкх";
        final String expectMessage = "Данная команда принимает [название категории] в одно или несколько слов.";
        categoryRepository.createUserCategory(this.mockUser, CategoryType.EXPENSE, categoryName);

        final List<String> emptyList = List.of();
        HandleCommandEvent command = new HandleCommandEvent(this.mockBot, this.mockUser, REMOVE_EXPENSE_COMMAND, emptyList);
        this.botHandler.handleCommand(command);
        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        MockMessage lastMessage = this.mockBot.poolMessageQueue();
        Assert.assertEquals(expectMessage, lastMessage.text());
    }

    /**
     * Создает пользователя для тестов
     * У него chatId = number, А баланс = 100 * number
     */
    private User createTestUser(int number) {
        assert number > 0;
        User user = new User(number, 100 * number);
        userRepository.saveUser(user);
        return user;
    }
}
