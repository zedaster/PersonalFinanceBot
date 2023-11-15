package ru.naumen.personalfinancebot.handler;

import org.hibernate.SessionFactory;
import org.junit.*;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;
import ru.naumen.personalfinancebot.repository.TestHibernateCategoryRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Тесты на команды для вывода категорий
 */
public class CategoryListTests {
    // Static необходим для инициализации данных перед тестами и очистки после всех тестов
    private static final SessionFactory sessionFactory;
    private static final UserRepository userRepository;
    private static final TestHibernateCategoryRepository categoryRepository;
    private static final OperationRepository operationRepository;

    private static final List<Category> standardCategories;
    private final BotHandler botHandler;

    /**
     * Моковый пользователь. Пересоздается для каждого теста
     */
    private User mockUser;

    /**
     * Моковый бот. Пересоздается для каждого теста.
     */
    private MockBot mockBot;

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

    public CategoryListTests() {
        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository);
    }

    /**
     * Очистка стандартных категорий и закрытие sessionFactory после выполнения всех тестов в этом классе
     */
    @AfterClass
    public static void finishTests() {
        categoryRepository.removeAll();
        sessionFactory.close();
    }

    /**
     * Создаем пользователя и бота перед каждым тестом
     */
    @Before
    public void beforeEachTest() {
        this.mockUser = new User(1L, 100);
        userRepository.saveUser(this.mockUser);
        this.mockBot = new MockBot();
    }

    /**
     * Удаляем пользователя из БД после каждого теста
     */
    @After
    public void afterEachTest() {
        userRepository.removeUserById(this.mockUser.getId());
    }

    /**
     * Тестирует отображение нескольких (трех) категорий доходов и нескольких (трех) категорий расходов
     */
    @Test
    public void showCoupleOfCategories() throws CategoryRepository.CreatingExistingCategoryException {
        showCategories(3);
    }

    /**
     * Тестирует отображение 100 категорий у пользователя (50 на расходы, 50 на доходы)
     * Должно быть отправлено одно длинное сообщение.
     * Бот его разделит сам при необходимости.
     */
    @Test
    public void showHundredCategories() throws CategoryRepository.CreatingExistingCategoryException {
        showCategories(50);
    }

    /**
     * Тестирует отображение по 1 категории на доход и расход у пользователя
     */
    @Test
    public void showOneCategory() throws CategoryRepository.CreatingExistingCategoryException {
        showCategories(1);
    }

    /**
     * Тестирует отображение пользовательских категорий при их отсутствии
     */
    @Test
    public void showNoCategories() throws CategoryRepository.CreatingExistingCategoryException {
        showCategories(0);
    }

    /**
     * Проверяет, отобразятся ли категории одного пользователя у другого
     */
    @Test
    public void privacyOfPersonalCategories() throws CategoryRepository.CreatingExistingCategoryException {
        User secondUser = new User(2L, 200.0);
        userRepository.saveUser(secondUser);

        List<Category> incomeCategories = addNumberedCategories(this.mockUser, CategoryType.INCOME, 3);
        List<String> expectedNames = this.getNumberedCategoriesNames(CategoryType.INCOME, 3);
        assertAllCommandExecution(this.mockUser, incomeCategories, expectedNames, List.of());
        assertAllCommandExecution(secondUser, List.of(), List.of(), List.of());

        incomeCategories.forEach((category -> {
            try {
                categoryRepository.removeCategoryById(category.getId());
            } catch (CategoryRepository.RemovingStandardCategoryException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    /**
     * Добавляет определенное количество категорий пользователю и проверяет работоспособность команд для
     * просмотра категорий (всех/только расходы/только доходы)
     * eachTypeCount - количество категорий для каждого типа.
     */
    private void showCategories(int eachTypeCount) throws CategoryRepository.CreatingExistingCategoryException {
        List<Category> incomeCategories = addNumberedCategories(this.mockUser, CategoryType.INCOME, eachTypeCount);
        List<Category> expenseCategories = addNumberedCategories(this.mockUser, CategoryType.EXPENSE, eachTypeCount);
        List<Category> allCategories = concatLists(incomeCategories, expenseCategories);

        List<String> expectedIncomeCategoryNames = this.getNumberedCategoriesNames(CategoryType.INCOME, eachTypeCount);
        List<String> expectedExpenseCategoryNames = this.getNumberedCategoriesNames(CategoryType.EXPENSE, eachTypeCount);

        assertAllCommandExecution(this.mockUser, allCategories, expectedIncomeCategoryNames, expectedExpenseCategoryNames);
        assertTypedCommandExecution(this.mockUser, CategoryType.INCOME, incomeCategories, expectedIncomeCategoryNames);
        assertTypedCommandExecution(this.mockUser, CategoryType.EXPENSE, expenseCategories, expectedExpenseCategoryNames);

        allCategories.forEach((category -> {
            try {
                categoryRepository.removeCategoryById(category.getId());
            } catch (CategoryRepository.RemovingStandardCategoryException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    /**
     * Проверяет выполнение команды для вывода категорий расходов (/list_expense_categories) или
     * категорий доходов (/list_income_categories)
     */
    private void assertTypedCommandExecution(User user, CategoryType type, List<Category> categories,
                                             List<String> expectNames) {
        Assert.assertEquals(categories.size(), expectNames.size());
        String expectedResponse = getExpectedResponse(type, expectNames);

        String commandName = "list_%s_categories".formatted(type.getCommandLabel());
        assertCommand(user, commandName, expectedResponse);
    }

    /**
     * Проверяет выполнение команды для вывода всех категорий расходов, доступных пользователю (/list_categories).
     */
    private void assertAllCommandExecution(User user, List<Category> categories,
                                           List<String> expectIncomeNames, List<String> expectExpenseNames) {
        Assert.assertEquals(expectIncomeNames.size() + expectExpenseNames.size(), categories.size());
        String expectedIncomesTypesResponse = getExpectedResponse(CategoryType.INCOME, expectIncomeNames);
        String expectedExpensesTypesResponse = getExpectedResponse(CategoryType.EXPENSE, expectExpenseNames);
        String expectedResponse = expectedIncomesTypesResponse + "\n" + expectedExpensesTypesResponse;

        assertCommand(user, "list_categories", expectedResponse);
    }

    /**
     * Проверяет выполнение команды commandName без аргументов для пользователя user с ожидаемым текстом сообщения
     * expectedResponse.
     */
    private void assertCommand(User user, String commandName, String expectedResponse) {
        HandleCommandEvent command = new HandleCommandEvent(this.mockBot, user, commandName, List.of());
        this.botHandler.handleCommand(command);
        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        MockMessage lastMessage = this.mockBot.poolMessageQueue();
        Assert.assertEquals(user, lastMessage.receiver());
        Assert.assertEquals(expectedResponse, lastMessage.text());
    }

    /**
     * Получает текст сообщения, которое ожидается для вывода категорий доходов/расходов.
     */
    private String getExpectedResponse(CategoryType type, List<String> expectNames) {
        String firstLine = "Все доступные вам категории %s: \n".formatted(type.getPluralShowLabel());
        StringBuilder expectedResponseBuilder = new StringBuilder(firstLine);

        expectedResponseBuilder.append("Стандартные: \n");
        List<Category> typedStandardCategories = standardCategories.stream()
                .filter(c -> c.getType() == type)
                .toList();
        for (int i = 0; i < typedStandardCategories.size(); i++) {
            expectedResponseBuilder
                    .append(i + 1)
                    .append(". ")
                    .append(typedStandardCategories.get(i).getCategoryName())
                    .append("\n");
        }

        expectedResponseBuilder.append("Персональные: \n");
        for (int i = 0; i < expectNames.size(); i++) {
            expectedResponseBuilder
                    .append(i + 1)
                    .append(". ")
                    .append(expectNames.get(i))
                    .append("\n");
        }
        return expectedResponseBuilder.toString();
    }


    /**
     * Создает и сохраняет категории в БД типа categoryType в количестве count штук.
     */
    private List<Category> addNumberedCategories(User testUser, CategoryType categoryType, int count)
            throws CategoryRepository.CreatingExistingCategoryException {
        List<Category> categories = new ArrayList<>();
        for (String name : getNumberedCategoriesNames(categoryType, count)) {
            Category category = categoryRepository.createUserCategory(testUser, categoryType, name);
            categories.add(category);
        }
        return categories;
    }

    /**
     * Создает названия для категории типа categoryType в количестве count штук.
     */
    private List<String> getNumberedCategoriesNames(CategoryType categoryType, int count) {
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String name = "Personal %s %d".formatted(categoryType.getCommandLabel(), i);
            names.add(name);
        }
        return names;
    }

    /**
     * Объединяет два списка в один
     */
    private <T> List<T> concatLists(List<T> list1, List<T> list2) {
        return Stream.concat(list1.stream(), list2.stream()).toList();
    }
}
