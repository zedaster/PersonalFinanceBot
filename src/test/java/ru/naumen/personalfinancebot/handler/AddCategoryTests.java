package ru.naumen.personalfinancebot.handler;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
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
import ru.naumen.personalfinancebot.repository.TestHibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.TestHibernateUserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Тесты для команды добавления категории
 */
public class AddCategoryTests {

    /**
     * Session factory для работы с сессиями в хранилищах
     */
    private static final SessionFactory sessionFactory;

    /**
     * Хранилище пользователей
     */
    private static final TestHibernateUserRepository userRepository;

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
     * Обработчик операций для бота
     */
    private final FinanceBotHandler botHandler;

    static {
        sessionFactory = new HibernateConfiguration().getSessionFactory();
        userRepository = new TestHibernateUserRepository(sessionFactory);
        categoryRepository = new TestHibernateCategoryRepository(sessionFactory);
        operationRepository = new HibernateOperationRepository(sessionFactory);
    }

    public AddCategoryTests() {
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

    @After
    public void afterEachTest() {
        userRepository.removeAll();
    }

    /**
     * Добавление корректных категорий
     */
    @Test
    public void addCorrectCategory() throws CategoryRepository.RemovingStandardCategoryException {
        final String[][] testCases = new String[][]{
                new String[]{"Taxi", "Taxi"},
                new String[]{"taxi", "Taxi"},
                new String[]{"tAxI", "Taxi"},
                new String[]{"Такси", "Такси"},
                new String[]{"1", "1"},
                new String[]{"A", "A"},
                new String[]{"Общая категория", "Общая категория"},
        };
        for (String[] testCase : testCases) {
            String value = testCase[0];
            String expected = testCase[1];
            for (CategoryType type : CategoryType.values()) {
                assertAddCorrectCategory(type, value, expected);
            }
        }
    }

    /**
     * Добавление некорректных категорий
     */
    @Test
    public void addIncorrectCategory() {
        final String some65chars = "fdafaresdakbmgernadsvckmbqteafvjickmblearfdsvmxcklrefbeafvdzxcmkf";
        final String[] testCases = new String[]{some65chars, "", " ", ".", "_", "так_неверно", "так,неправильно", "中文"};
        for (String value : testCases) {
            for (CategoryType type : CategoryType.values()) {
                assertIncorrectAddUserCategory(type, value);
            }
        }
    }

    /**
     * Добавление категории на расход и на доход с одним и тем же названием
     */
    @Test
    public void addSameIncomeAndExpense() throws CategoryRepository.RemovingStandardCategoryException {
        final String name = "Сервисы для помощи студентам";
        for (CategoryType type : CategoryType.values()) {
            assertAddCorrectCategory(type, name, name);
        }
    }

    /**
     * Проверка неверного количества аргументов для команды
     */
    @Test
    public void incorrectCountOfAddArguments() {
        List<List<String>> cases = List.of(
                List.of(),
                List.of(" ", ""),
                List.of(" ", " "),
                List.of("Two", "Args")
        );

        for (List<String> args : cases) {
            for (CategoryType type : CategoryType.values()) {
                User user = createTestUser(1);
                MockBot bot = executeAddCategoryCommand(user, type, args);
                Assert.assertEquals(1, bot.getMessageQueueSize());
                Assert.assertEquals(StaticMessages.INCORRECT_CATEGORY_ARGUMENT_COUNT, bot.poolMessageQueue().text());
                userRepository.removeUserById(user.getId());
            }
        }
    }

    /**
     * Тестирует, что категория добавиться только одному пользователю, а не двум
     */
    @Test
    public void twoUsers() {
        final String categoryName = "Зарплата";
        final CategoryType categoryType = CategoryType.INCOME;
        User user1 = createTestUser(1);
        User user2 = createTestUser(2);
        executeAddCategoryCommand(user1, categoryType, List.of(categoryName));
        categoryRepository.getCategoryByName(user1, categoryType, categoryName); // проверено ранее
        Optional<Category> shouldBeEmptyCategory = categoryRepository
                .getCategoryByName(user2, categoryType, categoryName);
        Assert.assertTrue(shouldBeEmptyCategory.isEmpty());
        userRepository.removeUserById(user1.getId());
        userRepository.removeUserById(user2.getId());
    }

    /**
     * Тестирует, что пользовательская категория, которая существует как стандартная, не будет добавлена.
     */
    @Test
    public void userAndStandardCategorySuppression() throws CategoryRepository.CreatingExistingCategoryException {
        final CategoryType categoryType = CategoryType.INCOME;
        final String categoryName = "Зарплата";
        categoryRepository.createStandardCategory(categoryType, categoryName);
        assertIncorrectAddUserCategory(categoryType, categoryName);
        categoryRepository.removeAll();
    }

    /**
     * Проводит тест для положительного кейса команды добавления категории
     */
    private void assertAddCorrectCategory(CategoryType type, String categoryName, String expectedCategoryName)
            throws CategoryRepository.RemovingStandardCategoryException {
        User user = createTestUser(1);
        Assert.assertTrue(categoryRepository.getCategoryByName(user, type, categoryName).isEmpty());
        executeAddCategoryCommand(user, type, List.of(categoryName));
        Optional<Category> addedCategory = categoryRepository.getCategoryByName(user, type, categoryName);
        Assert.assertTrue("Категория '%s' типа %s должна существовать".formatted(categoryName, type.name()),
                addedCategory.isPresent());
        Assert.assertEquals(addedCategory.get().getCategoryName(), expectedCategoryName);
        categoryRepository.removeCategoryById(addedCategory.get().getId());
        userRepository.removeUserById(user.getId());
    }


    /**
     * Проводит тест для отрицательного кейса команды добавления категории
     */
    private void assertIncorrectAddUserCategory(CategoryType type, String categoryName) {
        User user = createTestUser(1);
        executeAddCategoryCommand(user, type, List.of(categoryName));
        Optional<Category> addedCategory = categoryRepository.getCategoryByName(user, type, categoryName);
        Assert.assertTrue("Категория '%s' не должна быть добавлена как пользовательская".formatted(categoryName),
                addedCategory.isEmpty() || addedCategory.get().isStandard());
        userRepository.removeUserById(user.getId());
    }

    /**
     * Исполняет команду для добавления категории с переданными аргументами, учитывая тип категории.
     * Возвращает MockBot, с которым была исполнена команда
     */
    private MockBot executeAddCategoryCommand(User user, CategoryType type, List<String> args) {
        MockBot bot = new MockBot();
        String commandName = "add_" + type.getCommandLabel() + "_category";
        HandleCommandEvent command = new HandleCommandEvent(bot, user, commandName, args);
        botHandler.handleCommand(command);
        return bot;
    }

    /**
     * Создает пользователя для тестов
     * У него chatId = number, А баланс = number * 100
     */
    private User createTestUser(int number) {
        User user = new User(number, number * 100);
        userRepository.saveUser(user);
        return user;
    }
}
