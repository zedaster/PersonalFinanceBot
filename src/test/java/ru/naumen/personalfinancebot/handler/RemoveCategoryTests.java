package ru.naumen.personalfinancebot.handler;

import org.hibernate.SessionFactory;
import org.junit.*;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repositories.budget.HibernateBudgetRepository;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;
import ru.naumen.personalfinancebot.repository.hibernate.TestHibernateCategoryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RemoveCategoryTests {
    /**
     * Название моковой тестовой категории доходов, которая пересоздается перед каждым тестом.
     */
    private static final String TEST_USER_INCOME_CATEGORY_NAME = "Personal income 1"; // TODO: Переименовать

    /**
     * Название моковой тестовой категории расходов, которая пересоздается перед каждым тестом.
     */
    private static final String TEST_USER_EXPENSE_CATEGORY_NAME = "Personal expense 1";

    // Static необходим для инициализации данных перед тестами и очистки после всех тестов
    /**
     * Session factory для работы с сессиями в хранилищах
     */
    private static final SessionFactory sessionFactory;

    /**
     * Хранилище пользователей
     */
    private static final UserRepository userRepository;

    /**
     * Хранилище категорий
     * Данная реализация позволяет сделать полную очистку категорий после тестов
     */
    private static final TestHibernateCategoryRepository categoryRepository;

    /**
     * Хранилище операций
     */
    private static final OperationRepository operationRepository;

    /**
     * Стандартные категории. Создаются один раз для всего класса.
     */
    private static final List<Category> standardCategories;

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

    /**
     * Моковая тестовая категория дохода. Пересоздается перед каждым тестом
     */
    private Category testIncomeCategory;

    /**
     * Моковая тестовая категория расходов. Пересоздается перед каждым тестом
     */
    private Category testExpenseCategory;


    // Инициализация статических полей перед использованием класса
    static {
        sessionFactory = new HibernateConfiguration().getSessionFactory();
        userRepository = new HibernateUserRepository(sessionFactory);
        categoryRepository = new TestHibernateCategoryRepository(sessionFactory);
        operationRepository = new HibernateOperationRepository(sessionFactory);

        // Наполняем стандартные категории перед тестами
        Category[] categories = new Category[4];
        try {
            categories[0] = categoryRepository.createStandardCategory(CategoryType.INCOME, "Standard income 1");
            categories[1] = categoryRepository.createStandardCategory(CategoryType.INCOME, "Standard income 2");
            categories[2] = categoryRepository.createStandardCategory(CategoryType.EXPENSE, "Standard expense 1");
            categories[3] = categoryRepository.createStandardCategory(CategoryType.EXPENSE, "Standard expense 2");
        } catch (CategoryRepository.CreatingExistingCategoryException e) {
            throw new RuntimeException(e);
        }

        standardCategories = Arrays.stream(categories).toList();
    }

    public RemoveCategoryTests() {
        BudgetRepository budgetRepository = new HibernateBudgetRepository(sessionFactory);
        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository, budgetRepository);
    }

    /**
     * Очистка стандартных значений и закрытие sessionFactory после выполнения всех тестов в этом классе
     */
    @AfterClass
    public static void finishTests() {
        categoryRepository.removeAll();
        sessionFactory.close();
    }

    /**
     * Создаем пользователя и бота перед каждым тестом
     * У пользователя будут категории Personal Income 1 и Personal Expense 1
     */
    @Before
    public void beforeEachTest() throws CategoryRepository.CreatingExistingCategoryException {
        this.mockUser = createTestUser(1);
        this.testIncomeCategory = categoryRepository
                .createUserCategory(this.mockUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);
        this.testExpenseCategory = categoryRepository
                .createUserCategory(this.mockUser, CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);
        this.mockBot = new MockBot();
    }

    /**
     * Удаляем пользователя из БД и его категории после каждого теста
     */
    @After
    public void afterEachTest() throws CategoryRepository.RemovingStandardCategoryException {
        categoryRepository.removeCategoryById(testIncomeCategory.getId());
        categoryRepository.removeCategoryById(testExpenseCategory.getId());
        userRepository.removeUserById(this.mockUser.getId());
    }

    /**
     * Тестирует удаление одной из существующих категорий расходов.
     */
    @Test
    public void removeOneOfOneExistingExpenseCategory() {
        executeRemoveCommandWithOneArg(this.mockUser, CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);

        assertUserCategoryNotExists(this.mockUser, CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);
        assertUserCategoryExists(this.mockUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);
    }

    /**
     * Тестирует удаление одной из существующих категорий доходов.
     */
    @Test
    public void removeOneOfOneExistingIncomeCategory() {
        executeRemoveCommandWithOneArg(this.mockUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);

        assertUserCategoryExists(this.mockUser, CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);
        assertUserCategoryNotExists(this.mockUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);
    }

    /**
     * Тестирует удаление одной из существующих категорий.
     */
    @Test
    public void removeOneOfTwoExistingCategories() throws CategoryRepository.RemovingStandardCategoryException,
            CategoryRepository.CreatingExistingCategoryException {
        final String secondExpenseCategoryName = TEST_USER_INCOME_CATEGORY_NAME + "1";
        Category secondExpenseCategory = categoryRepository
                .createUserCategory(this.mockUser, CategoryType.EXPENSE, secondExpenseCategoryName);

        executeRemoveCommandWithOneArg(this.mockUser, CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);

        assertUserCategoryNotExists(this.mockUser, CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);
        assertUserCategoryExists(this.mockUser, CategoryType.EXPENSE, secondExpenseCategoryName);
        assertUserCategoryExists(this.mockUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);

        categoryRepository.removeCategoryById(secondExpenseCategory.getId());
    }

    /**
     * Тестирует невозможность удаления несуществующих категорий.
     */
    @Test
    public void removeNonExistingCategory() {
        final String noNameCategory = "No name";
        executeRemoveCommandWithOneArg(this.mockUser, CategoryType.EXPENSE, noNameCategory);

        assertUserCategoryNotExists(this.mockUser, CategoryType.EXPENSE, noNameCategory);
        assertUserCategoryExists(this.mockUser, CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);
        assertUserCategoryExists(this.mockUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);
        Assert.assertEquals(this.mockBot.getMessageQueueSize(), 1);
        MockMessage lastMessage = this.mockBot.poolMessageQueue();
        Assert.assertEquals(this.mockUser, lastMessage.receiver());
        Assert.assertEquals(StaticMessages.USER_CATEGORY_ALREADY_NOT_EXISTS
                        .replace("{type}", CategoryType.EXPENSE.getPluralShowLabel())
                        .replace("{name}", noNameCategory),
                lastMessage.text());
    }

    /**
     * Тестирует возможность удаления существующих категорий вне зависимости от регистра названия
     */
    @Test
    public void removeIgnoreCase() {
        String testCategoryName = randomizeCase(TEST_USER_EXPENSE_CATEGORY_NAME);

        executeRemoveCommandWithOneArg(this.mockUser, CategoryType.EXPENSE, testCategoryName);

        assertUserCategoryNotExists(this.mockUser, CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);
        assertUserCategoryExists(this.mockUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);
    }

    /**
     * Тестирует невозможность удаления категории, которая существует, но с другим типом.
     */
    @Test
    public void removeWrongTypeCategory() throws CategoryRepository.RemovingStandardCategoryException,
            CategoryRepository.CreatingExistingCategoryException {
        final String testSameCategoryName = "Same category";

        Category expenseCategory = categoryRepository.createUserCategory(this.mockUser, CategoryType.EXPENSE,
                testSameCategoryName);
        Category incomeCategory = categoryRepository.createUserCategory(this.mockUser, CategoryType.INCOME,
                testSameCategoryName);

        executeRemoveCommandWithOneArg(this.mockUser, CategoryType.EXPENSE, testSameCategoryName);

        assertUserCategoryExists(this.mockUser, CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);
        assertUserCategoryExists(this.mockUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);
        assertUserCategoryNotExists(this.mockUser, CategoryType.EXPENSE, testSameCategoryName);
        assertUserCategoryExists(this.mockUser, CategoryType.INCOME, testSameCategoryName);

        categoryRepository.removeCategoryById(expenseCategory.getId());
        categoryRepository.removeCategoryById(incomeCategory.getId());
    }

    /**
     * Тестирует удаление категории у одного пользователя, когда у еще одного пользователя есть категория с тем же
     * названием и типом.
     */
    @Test
    public void removeExistingCategoryWithTwoUsers() throws CategoryRepository.RemovingStandardCategoryException,
            CategoryRepository.CreatingExistingCategoryException {
        final String testSameIncomeCategoryName = "Same category";

        User secondUser = createTestUser(2);
        Category firstUserCategory = categoryRepository.createUserCategory(this.mockUser, CategoryType.INCOME,
                testSameIncomeCategoryName);
        Category secondUserCategory = categoryRepository.createUserCategory(secondUser, CategoryType.INCOME,
                testSameIncomeCategoryName);

        executeRemoveCommandWithOneArg(this.mockUser, CategoryType.INCOME, testSameIncomeCategoryName);

        assertUserCategoryExists(this.mockUser, CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);
        assertUserCategoryExists(this.mockUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);
        assertUserCategoryNotExists(this.mockUser, CategoryType.INCOME, testSameIncomeCategoryName);
        assertUserCategoryNotExists(secondUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);
        assertUserCategoryNotExists(secondUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);
        assertUserCategoryExists(secondUser, CategoryType.INCOME, testSameIncomeCategoryName);

        categoryRepository.removeCategoryById(firstUserCategory.getId());
        categoryRepository.removeCategoryById(secondUserCategory.getId());
        userRepository.removeUserById(secondUser.getId());
    }

    /**
     * Тестирует невозможность удаления стандартной категории пользователем.
     */
    @Test
    public void removeStandardCategoryByUser() {
        Category someStandardCategory = standardCategories.get(0);

        executeRemoveCommandWithOneArg(this.mockUser, someStandardCategory.getType(), someStandardCategory.getCategoryName());

        assertStandardCategoryExists(someStandardCategory.getType(), someStandardCategory.getCategoryName());
        assertUserCategoryExists(this.mockUser, CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);
        assertUserCategoryExists(this.mockUser, CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);
        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        MockMessage lastMessage = this.mockBot.poolMessageQueue();
        Assert.assertEquals(this.mockUser, lastMessage.receiver());
        String expectedMessage = StaticMessages.USER_CATEGORY_ALREADY_NOT_EXISTS
                .replace("{type}", CategoryType.INCOME.getPluralShowLabel())
                .replace("{name}", someStandardCategory.getCategoryName());
        Assert.assertEquals(expectedMessage, lastMessage.text());
    }

    /**
     * Тестирует невозможность удаления категории расходов, если аргументы введены неверно
     */
    @Test
    public void removeExpenseCategoryWrongArgs() {
        removeWrongArgs(CategoryType.EXPENSE, TEST_USER_EXPENSE_CATEGORY_NAME);
    }

    /**
     * Тестирует невозможность удаления категории доходов, если аргументы введены неверно
     */
    @Test
    public void removeIncomeCategoryWrongArgs() {
        removeWrongArgs(CategoryType.INCOME, TEST_USER_INCOME_CATEGORY_NAME);
    }

    /**
     * Тестирует невозможность удаления определенной категории, если аргументы введены неверно
     */
    private void removeWrongArgs(CategoryType type, String testCategoryName) {
        List<List<String>> wrongArgsCases = List.of(
                List.of(),
                List.of("ARG1", testCategoryName),
                List.of(testCategoryName, "ARG2"),
                List.of("ARG1", "ARG2")
        );

        String cmdName = "remove_" + type.getCommandLabel() + "_category";
        for (List<String> args : wrongArgsCases) {
            HandleCommandEvent command = new HandleCommandEvent(this.mockBot, this.mockUser, cmdName, args);
            this.botHandler.handleCommand(command);
            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage lastMessage = this.mockBot.poolMessageQueue();
            Assert.assertEquals(this.mockUser, lastMessage.receiver());
            Assert.assertEquals(StaticMessages.INCORRECT_CATEGORY_ARGUMENT_COUNT, lastMessage.text());
        }
    }

    /**
     * Выполняет команду удаления, если правильно введен один аргумент - название категории
     */
    private void executeRemoveCommandWithOneArg(User user, CategoryType type, String argument) {
        String cmdName = "remove_" + type.getCommandLabel() + "_category";
        List<String> args = List.of(argument);
        HandleCommandEvent command = new HandleCommandEvent(this.mockBot, user, cmdName, args);
        this.botHandler.handleCommand(command);
    }

    /**
     * Тестирует, есть ли такая пользовательская категория
     */
    private void assertUserCategoryExists(User user, CategoryType type, String categoryName) {
        Assert.assertTrue(categoryRepository
                .getCategoryByName(user, type, categoryName)
                .isPresent());
    }

    /**
     * Тестирует, есть ли такая стандартная категория
     */
    private void assertStandardCategoryExists(CategoryType type, String categoryName) {
        Assert.assertTrue(categoryRepository
                .getStandardCategoryByName(type, categoryName)
                .isPresent());
    }

    /**
     * Тестирует, отсутствует ли такая пользовательская категория
     */
    private void assertUserCategoryNotExists(User user, CategoryType type, String categoryName) {
        Optional<Category> category = categoryRepository.getCategoryByName(user, type, categoryName);
        Assert.assertTrue(category.isEmpty() || category.get().isStandard());
    }

    /**
     * Меняет у строки все буквы на большие или маленькие посимвольно случайным образом.
     */
    private String randomizeCase(String value) {
        char[] valueCharArray = value.toCharArray();
        char[] resultCharArray = new char[value.length()];
        Random random = new Random();
        for (int i = 0; i < value.length(); i++) {
            if (random.nextBoolean()) {
                resultCharArray[i] = Character.toLowerCase(valueCharArray[i]);
            } else {
                resultCharArray[i] = Character.toUpperCase(valueCharArray[i]);
            }
        }
        return String.valueOf(resultCharArray);
    }

    /**
     * Создает пользователя для тестов
     * У него chatId = number, А баланс = 100 * number
     */
    private static User createTestUser(int number) {
        assert number > 0;
        User user = new User(number, 100 * number);
        userRepository.saveUser(user);
        return user;
    }
}
