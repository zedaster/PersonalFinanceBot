package ru.naumen.personalfinancebot.handler.command.budget;

import org.hibernate.SessionFactory;
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
import ru.naumen.personalfinancebot.repository.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repository.empty.EmptyCategoryRepository;
import ru.naumen.personalfinancebot.repository.empty.EmptyUserRepository;
import ru.naumen.personalfinancebot.repository.fake.FakeBudgetRepository;
import ru.naumen.personalfinancebot.repository.fake.FakeOperationRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;

import java.time.YearMonth;
import java.util.List;

/**
 * Тесты для команды "/budget"
 */
public class SingleBudgetTests {
    /**
     * Фабрика сессий
     */
    private final SessionFactory sessionFactory;

    /**
     * Менеджер транзакций
     */
    private final TransactionManager transactionManager;

    /**
     * Экземпляр класс фейковой реализации бота
     */
    private MockBot mockBot;

    /**
     * Обработчик операций бота
     */
    private FinanceBotHandler botHandler;

    /**
     * Хранилище операций
     */
    private OperationRepository operationRepository;

    /**
     * Хранилище бюджетов
     */
    private BudgetRepository budgetRepository;

    /**
     * Пользователь
     */
    private User user;

    public SingleBudgetTests() {
        this.sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.transactionManager = new TransactionManager(this.sessionFactory);
    }

    /**
     * Переинциализация объектов перед каждым тестом
     */
    @Before
    public void initVariables() {
        this.operationRepository = new FakeOperationRepository();
        this.budgetRepository = new FakeBudgetRepository();
        this.mockBot = new MockBot();
        this.botHandler = new FinanceBotHandler(
                new EmptyUserRepository(),
                this.operationRepository,
                new EmptyCategoryRepository(),
                this.budgetRepository,
                sessionFactory
        );
        this.user = new User(1, 100);
    }

    /**
     * Проверка вывода существующего бюджета при отсутствующих операциях
     */
    @Test
    public void existingBudgetNoOperations() {
        transactionManager.produceTransaction(session -> {
            TestYearMonth testYearMonth = TestYearMonth.current();
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
            TestYearMonth testYearMonth = TestYearMonth.current();

            Budget budget = new Budget(this.user, 100_000, 90_000, testYearMonth.getYearMonth());
            this.budgetRepository.saveBudget(session, budget);
            Category fakeIncomeCategory = new Category(this.user, "Fake", CategoryType.INCOME);
            Category fakeExpenseCategory = new Category(this.user, "Fake", CategoryType.EXPENSE);
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
            Assert.assertEquals(this.user, message.receiver());
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

            TestYearMonth testYM = TestYearMonth.current();
            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("""
                Бюджет на %s %d отсутствует""".formatted(testYM.getMonthName(), testYM.getYear()), message.text());
            Assert.assertEquals(this.user, message.receiver());
        });
    }
}
