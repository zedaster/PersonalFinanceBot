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
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.TestHibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.TestHibernateUserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Тесты на команды для вывода категорий
 */
public class CategoryListTests {
    // Static необходим для инициализации данных перед тестами и очистки после всех тестов

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
     * Обработчик команд для бота
     */
    private final FinanceBotHandler botHandler;

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
        userRepository = new TestHibernateUserRepository(sessionFactory);
        categoryRepository = new TestHibernateCategoryRepository(sessionFactory);
        operationRepository = new HibernateOperationRepository(sessionFactory);

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
     * Удаляем всех пользователей из БД после каждого теста
     */
    @After
    public void afterEachTest() {
        userRepository.removeAll();
    }

    /**
     * Тестирует отображение нескольких (трех) категорий доходов и нескольких (трех) категорий расходов
     */
    @Test
    public void showCoupleOfCategories() throws CategoryRepository.CreatingExistingCategoryException {
        ExpectedCategoryFormat expect = new ExpectedCategoryFormat(
                """
                        1. Standard income 1
                        2. Standard income 2
                        """,
                """
                        1. Standard expense 1
                        2. Standard expense 2
                        """,
                """
                        1. Personal income 1
                        2. Personal income 2
                        3. Personal income 3
                        """,
                """
                        1. Personal expense 1
                        2. Personal expense 2
                        3. Personal expense 3
                        """
        );

        assertShowCategories(3, expect);
    }

    /**
     * Тестирует отображение 100 категорий у пользователя (50 на расходы, 50 на доходы)
     * Должно быть отправлено одно длинное сообщение.
     * Бот его разделит сам при необходимости.
     */
    @Test
    public void showHundredCategories() throws CategoryRepository.CreatingExistingCategoryException {
        ExpectedCategoryFormat expect = new ExpectedCategoryFormat(
                """
                        1. Standard income 1
                        2. Standard income 2
                        """,
                """
                        1. Standard expense 1
                        2. Standard expense 2
                        """,
                getExpectPersonalCategoriesFormat(CategoryType.INCOME, 50),
                getExpectPersonalCategoriesFormat(CategoryType.EXPENSE, 50)
        );

        assertShowCategories(50, expect);
    }

    /**
     * Генерирует и выводит ожидаемый нумерованный список в виде строки
     */
    private String getExpectPersonalCategoriesFormat(CategoryType type, int count) {
        List<String> namesList = this.getPersonalCategoriesNames(type, count);
        return IntStream.range(0, count)
                .mapToObj(i -> (i + 1) + ". " + namesList.get(i) + "\n")
                .collect(Collectors.joining());
    }

    /**
     * Тестирует отображение по 1 категории на доход и расход у пользователя
     */
    @Test
    public void showOneCategory() throws CategoryRepository.CreatingExistingCategoryException {
        ExpectedCategoryFormat expect = new ExpectedCategoryFormat(
                """
                        1. Standard income 1
                        2. Standard income 2
                        """,
                """
                        1. Standard expense 1
                        2. Standard expense 2
                        """,
                """
                        1. Personal income 1
                        """,
                """
                        1. Personal expense 1
                        """
        );
        assertShowCategories(1, expect);
    }

    /**
     * Тестирует отображение пользовательских категорий при их отсутствии
     */
    @Test
    public void showNoCategories() throws CategoryRepository.CreatingExistingCategoryException {
        ExpectedCategoryFormat expect = new ExpectedCategoryFormat(
                """
                        1. Standard income 1
                        2. Standard income 2
                        """,
                """
                        1. Standard expense 1
                        2. Standard expense 2
                        """,
                StaticMessages.EMPTY_LIST_CONTENT + "\n",
                StaticMessages.EMPTY_LIST_CONTENT + "\n"
        );
        assertShowCategories(0, expect);
    }

    /**
     * Проверяет, отобразятся ли категории одного пользователя у другого
     */
    @Test
    public void privacyOfPersonalCategories() throws CategoryRepository.CreatingExistingCategoryException {
        User secondUser = new User(2L, 200.0);
        userRepository.saveUser(secondUser);

        List<Category> incomeCategories = addNumberedCategories(this.mockUser, CategoryType.INCOME, 3);

        ExpectedCategoryFormat expectMockUserFormat = new ExpectedCategoryFormat(
                """
                        1. Standard income 1
                        2. Standard income 2
                        """,
                """
                        1. Standard expense 1
                        2. Standard expense 2
                        """,
                """
                        1. Personal income 1
                        2. Personal income 2
                        3. Personal income 3
                        """,
                StaticMessages.EMPTY_LIST_CONTENT + "\n"
        );
        ExpectedCategoryFormat expectSecondUserFormat = new ExpectedCategoryFormat(
                """
                        1. Standard income 1
                        2. Standard income 2
                        """,
                """
                        1. Standard expense 1
                        2. Standard expense 2
                        """,
                StaticMessages.EMPTY_LIST_CONTENT + "\n",
                StaticMessages.EMPTY_LIST_CONTENT + "\n"
        );
        assertAllCommandExecution(this.mockUser, expectMockUserFormat);
        assertAllCommandExecution(secondUser, expectSecondUserFormat);

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
     *
     * @param eachTypeCount количество категорий для каждого типа
     * @param expectFormat ожидаемые сообщения
     */
    private void assertShowCategories(int eachTypeCount, ExpectedCategoryFormat expectFormat)
            throws CategoryRepository.CreatingExistingCategoryException {
        List<Category> incomeCategories = addNumberedCategories(this.mockUser, CategoryType.INCOME, eachTypeCount);
        List<Category> expenseCategories = addNumberedCategories(this.mockUser, CategoryType.EXPENSE, eachTypeCount);
        List<Category> allCategories = concatLists(incomeCategories, expenseCategories);

        assertAllCommandExecution(this.mockUser, expectFormat);
        assertTypedCommandExecution(this.mockUser, CategoryType.INCOME,
                expectFormat.standardIncomeCategories, expectFormat.personalIncomeCategories);
        assertTypedCommandExecution(this.mockUser, CategoryType.EXPENSE, expectFormat.standardExpenseCategories,
                expectFormat.personalExpenseCategories);

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
    private void assertTypedCommandExecution(User user, CategoryType type, String expectStandardListFormat,
                                             String expectPersonalListFormat) {
        String expectedResponse = getExpectedResponse(type, expectStandardListFormat,
                expectPersonalListFormat);

        String commandName = "list_%s_categories".formatted(type.getCommandLabel());
        assertCommand(user, commandName, expectedResponse);
    }

    /**
     * Проверяет выполнение команды для вывода всех категорий расходов, доступных пользователю (/list_categories).
     */
    private void assertAllCommandExecution(User user, ExpectedCategoryFormat expectFormat) {
        String expectedIncomesTypesResponse = getExpectedResponse(CategoryType.INCOME,
                expectFormat.standardIncomeCategories(), expectFormat.personalIncomeCategories());
        String expectedExpensesTypesResponse = getExpectedResponse(CategoryType.EXPENSE,
                expectFormat.standardExpenseCategories(), expectFormat.personalExpenseCategories());
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
    private String getExpectedResponse(CategoryType type, String expectStandardListFormat, String expectPersonalListFormat) {
        return StaticMessages.LIST_TYPED_CATEGORIES
                .replace("{type}", type.getPluralShowLabel())
                .replace("{standard_list}", expectStandardListFormat)
                .replace("{personal_list}", expectPersonalListFormat);
    }


    /**
     * Создает и сохраняет пользовательские категории в БД типа categoryType в количестве count штук.
     */
    private List<Category> addNumberedCategories(User testUser, CategoryType categoryType, int count)
            throws CategoryRepository.CreatingExistingCategoryException {
        List<Category> categories = new ArrayList<>();
        for (String name : getPersonalCategoriesNames(categoryType, count)) {
            Category category = categoryRepository.createUserCategory(testUser, categoryType, name);
            categories.add(category);
        }
        return categories;
    }

    /**
     * Создает названия для пользовательских категорий типа categoryType в количестве count штук.
     */
    private List<String> getPersonalCategoriesNames(CategoryType categoryType, int count) {
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

    /**
     * Ожидаемые строковые значения нумерованного списка категорий при вызове команды
     */
    private record ExpectedCategoryFormat(String standardIncomeCategories,
                                          String standardExpenseCategories,
                                          String personalIncomeCategories,
                                          String personalExpenseCategories) {
    }
}
