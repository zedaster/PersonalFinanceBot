package ru.naumen.personalfinancebot.handler;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Тесты для команды добавления категории
 */
public class AddCategoryTests {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final OperationRepository operationRepository;
    private final BotHandler botHandler;

    public AddCategoryTests() {
        SessionFactory sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.userRepository = new HibernateUserRepository(sessionFactory);
        this.categoryRepository = new HibernateCategoryRepository(sessionFactory);
        this.operationRepository = new HibernateOperationRepository(sessionFactory);
        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository);
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
                assertAddIncorrectCategoryName(type, value);
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
                User user = createFirstTestUser();
                MockBot bot = executeAddCategoryCommand(user, type, args);
                Assert.assertEquals(1, bot.getMessageQueueSize());
                Assert.assertEquals("Данная команда принимает 1 аргумент: [название категории]",
                        bot.poolMessageQueue().text());
                this.userRepository.removeUserById(user.getId());
            }
        }
    }

    /**
     * Тестирует, что категория добавиться только одному пользователю, а не двум
     */
    @Test
    public void twoUsers() throws CategoryRepository.RemovingStandardCategoryException {
        final String categoryName = "Зарплата";
        final CategoryType categoryType = CategoryType.INCOME;
        User user1 = createFirstTestUser();
        User user2 = createSecondTestUser();
        executeAddCategoryCommand(user1, categoryType, List.of(categoryName));
        Optional<Category> addedFirstCategory = this.categoryRepository.getUserCategoryByName(user1, categoryType, categoryName);
        Assert.assertTrue(addedFirstCategory.isPresent());
        Optional<Category> addedCategory = this.categoryRepository.getUserCategoryByName(user2, categoryType, categoryName);
        Assert.assertTrue(addedCategory.isEmpty());
        this.categoryRepository.removeCategoryById(addedFirstCategory.get().getId());
        this.userRepository.removeUserById(user1.getId());
        this.userRepository.removeUserById(user2.getId());
    }

    /**
     * Тестирует, что пользовательская категория, которая существует как стандартная, не будет добавлена.
     */
    @Test
    public void userAndStandardCategorySuppression() throws CategoryRepository.CreatingExistingCategoryException {
        final CategoryType categoryType = CategoryType.INCOME;
        final String categoryName = "Зарплата";
        this.categoryRepository.createStandardCategory(categoryType, categoryName);
        assertAddIncorrectCategoryName(categoryType, categoryName);
    }

    /**
     * Проводит тест для положительного кейса команды добавления категории
     */
    private void assertAddCorrectCategory(CategoryType type, String categoryName, String expectedCategoryName)
            throws CategoryRepository.RemovingStandardCategoryException {
        User user = createFirstTestUser();
        Assert.assertTrue(this.categoryRepository.getUserCategoryByName(user, type, categoryName).isEmpty());
        executeAddCategoryCommand(user, type, List.of(categoryName));
        Optional<Category> addedCategory = this.categoryRepository.getUserCategoryByName(user, type, categoryName);
        Assert.assertTrue("Категория '%s' типа %s должна существовать".formatted(categoryName, type.name()),
                addedCategory.isPresent());
        Assert.assertEquals(addedCategory.get().getCategoryName(), expectedCategoryName);
        this.categoryRepository.removeCategoryById(addedCategory.get().getId());
        this.userRepository.removeUserById(user.getId());
    }


    /**
     * Проводит тест для отрицательного кейса команды добавления категории
     */
    private void assertAddIncorrectCategoryName(CategoryType type, String categoryName) {
        User user = createFirstTestUser();
        Assert.assertTrue(this.categoryRepository.getUserCategoryByName(user, type, categoryName).isEmpty());
        executeAddCategoryCommand(user, type, List.of(categoryName));
        Optional<Category> addedCategory = this.categoryRepository.getUserCategoryByName(user, type, categoryName);
        Assert.assertTrue("Категория '%s' не должна быть добавлена".formatted(categoryName),
                addedCategory.isEmpty());
        this.userRepository.removeUserById(user.getId());
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
     * Создает первого пользователя для тестов
     * У него chatId = 1L, А баланс = 100.0
     */
    private User createFirstTestUser() {
        User user = new User(1L, 100.0);
        this.userRepository.saveUser(user);
        return user;
    }

    /**
     * Создает второго пользователя для тестов
     * У него chatId = 2L, А баланс = 200.0
     */
    private User createSecondTestUser() {
        User user = new User(2L, 200.0);
        this.userRepository.saveUser(user);
        return user;
    }

}
