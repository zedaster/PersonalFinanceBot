package ru.naumen.personalfinancebot.handler.command.budget;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.FinanceBotHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.TransactionManager;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingStandardCategoryException;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingUserCategoryException;
import ru.naumen.personalfinancebot.repository.hibernate.TestHibernateBudgetRepository;
import ru.naumen.personalfinancebot.repository.hibernate.TestHibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.hibernate.TestHibernateOperationRepository;
import ru.naumen.personalfinancebot.repository.hibernate.TestHibernateUserRepository;

import java.time.YearMonth;
import java.util.List;

/**
 * Тесты для команды "/budget"
 */
public class SingleBudgetTest {
    /**
     * Менеджер транзакций
     */
    private final TransactionManager transactionManager;

    /**
     * Обработчик операций бота
     */
    private final FinanceBotHandler botHandler;

    /**
     * Хранилище пользователей
     */
    private final TestHibernateUserRepository userRepository;

    /**
     * Хранилище категорий
     */
    private final TestHibernateCategoryRepository categoryRepository;

    /**
     * Хранилище операций
     */
    private final TestHibernateOperationRepository operationRepository;

    /**
     * Хранилище бюджетов
     */
    private final TestHibernateBudgetRepository budgetRepository;

    /**
     * Фейковая категория доходов
     */
    private Category fakeIncomeCategory;

    /**
     * Фейковая категория расходов
     */
    private Category fakeExpenseCategory;

    /**
     * Экземпляр класс фейковой реализации бота
     */
    private MockBot mockBot;

    /**
     * Пользователь
     */
    private User user;

    public SingleBudgetTest() {
        SessionFactory sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.transactionManager = new TransactionManager(sessionFactory);
        this.userRepository = new TestHibernateUserRepository();
        this.categoryRepository = new TestHibernateCategoryRepository();
        this.operationRepository = new TestHibernateOperationRepository();
        this.budgetRepository = new TestHibernateBudgetRepository();
        this.botHandler = new FinanceBotHandler(
                this.userRepository,
                this.operationRepository,
                this.categoryRepository,
                this.budgetRepository);
    }

    /**
     * Переинциализация объектов перед каждым тестом
     */
    @Before
    public void initVariables() {
        this.mockBot = new MockBot();
        this.user = new User(1, 100);
        this.transactionManager.produceTransaction(session -> {
            this.userRepository.saveUser(session, this.user);
        });
        addFakeStandardCategories();
    }

    /**
     * Очистка репозиториев после каждого теста
     */
    @After
    public void clearRepositories() {
        this.transactionManager.produceTransaction(session -> {
            this.budgetRepository.removeAll(session);
            this.operationRepository.removeAll(session);
            this.categoryRepository.removeAll(session);
            this.userRepository.removeAll(session);
        });
    }

    /**
     * Проверка вывода существующего бюджета при отсутствующих операциях
     * И отправление итогового сообщения нужному пользователю.
     */
    @Test
    public void existingBudgetNoOperations() {
        transactionManager.produceTransaction(session -> {
            TestYearMonth testYearMonth = new TestYearMonth();
            Budget budget = new Budget(this.user, 100_000, 90_000, testYearMonth.getYearMonth());
            this.budgetRepository.saveBudget(session, budget);
            CommandData command = new CommandData(this.mockBot, this.user, "budget", List.of());
            this.botHandler.handleCommand(command, session);

            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("""
                        Бюджет на %s %d:
                        Ожидаемые доходы: 100 000
                        Ожидаемые расходы: 90 000
                        Текущие доходы: 0
                        Текущие расходы: 0
                        Текущий баланс: 100
                        Нужно еще заработать: 100 000
                        Еще осталось на траты: 90 000""".formatted(
                            testYearMonth.getMonthName(),
                            testYearMonth.getYear()),
                    message.text());
            Assert.assertEquals(this.user, message.receiver());
        });
    }

    /**
     * Проверка вывода существующего бюджета при существующих операциях
     */
    @Test
    public void existingBudgetAndOperations() {
        transactionManager.produceTransaction(session -> {
            TestYearMonth testYearMonth = new TestYearMonth();

            Budget budget = new Budget(this.user, 100_000, 90_000, testYearMonth.getYearMonth());
            this.budgetRepository.saveBudget(session, budget);

            this.operationRepository.addOperation(session, this.user, fakeIncomeCategory, 3000);
            this.operationRepository.addOperation(session, this.user, fakeExpenseCategory, 1000);

            CommandData command = new CommandData(this.mockBot, this.user, "budget", List.of());
            this.botHandler.handleCommand(command, session);

            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("""
                Бюджет на %s %d:
                Ожидаемые доходы: 100 000
                Ожидаемые расходы: 90 000
                Текущие доходы: 3 000
                Текущие расходы: 1 000
                Текущий баланс: 100
                Нужно еще заработать: 97 000
                Еще осталось на траты: 89 000""".formatted(
                    testYearMonth.getMonthName(),
                    testYearMonth.getYear()), message.text());
        });
    }

    /**
     * Тестирует случай, когда реальный доход и расход превысил ожидаемые
     */
    @Test
    public void realOperationsExceededExpected() {
        transactionManager.produceTransaction(session -> {
            TestYearMonth testYearMonth = new TestYearMonth();

            Budget budget = new Budget(this.user, 100_000, 90_000, testYearMonth.getYearMonth());
            this.budgetRepository.saveBudget(session, budget);

            this.operationRepository.addOperation(session, this.user, fakeIncomeCategory, 101_000);
            this.operationRepository.addOperation(session, this.user, fakeExpenseCategory, 91_000);

            CommandData command = new CommandData(this.mockBot, this.user, "budget", List.of());
            this.botHandler.handleCommand(command, session);

            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("""
                    Бюджет на %s %d:
                    Ожидаемые доходы: 100 000
                    Ожидаемые расходы: 90 000
                    Текущие доходы: 101 000
                    Текущие расходы: 91 000
                    Текущий баланс: 100
                    Нужно еще заработать: 0
                    Еще осталось на траты: 0""".formatted(
                    testYearMonth.getMonthName(),
                    testYearMonth.getYear()), message.text());
        });
    }

    /**
     * Проверка вывода текущего бюджета при его отсутствии и присутствии бюджета на другой период в прошлом
     */
    @Test
    public void noCurrentBudget() {
        transactionManager.produceTransaction(session -> {
            Budget budget = new Budget(this.user, 100_000, 90_000, YearMonth.of(2020, 1));
            this.budgetRepository.saveBudget(session, budget);
            CommandData command = new CommandData(this.mockBot, this.user, "budget", List.of());
            this.botHandler.handleCommand(command, session);

            TestYearMonth testYM = new TestYearMonth();
            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("""
                Бюджет на %s %d отсутствует""".formatted(testYM.getMonthName(), testYM.getYear()), message.text());
            Assert.assertEquals(this.user, message.receiver());
        });
    }

    /**
     * Создает стандартные категории для тестов
     */
    private void addFakeStandardCategories() {
        transactionManager.produceTransaction(session -> {
            try {
                this.fakeIncomeCategory = this.categoryRepository
                        .createUserCategory(session, this.user, CategoryType.INCOME, "Fake");
                this.fakeExpenseCategory = this.categoryRepository
                        .createUserCategory(session, this.user, CategoryType.EXPENSE, "Fake");
            } catch (ExistingStandardCategoryException | ExistingUserCategoryException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
