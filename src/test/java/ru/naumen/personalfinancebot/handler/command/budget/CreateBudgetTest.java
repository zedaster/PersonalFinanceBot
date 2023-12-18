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

import java.util.List;

/**
 * Тесты для команды "/budget_create".
 */
public class CreateBudgetTest {
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
     * Хранилище бюджетов
     */
    private BudgetRepository budgetRepository;

    /**
     * Хранилище опреаций
     */
    private OperationRepository operationRepository;

    /**
     * Пользователь
     */
    private User user;

    public CreateBudgetTest() {
        this.sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.transactionManager = new TransactionManager(this.sessionFactory);
    }

    /**
     * Переинциализация объектов перед каждым тестом
     */
    @Before
    public void initVariables() {
        this.budgetRepository = new FakeBudgetRepository();
        this.operationRepository = new FakeOperationRepository();
        this.mockBot = new MockBot();
        this.botHandler = new FinanceBotHandler(
                new EmptyUserRepository(),
                this.operationRepository,
                new EmptyCategoryRepository(),
                this.budgetRepository,
                this.sessionFactory
        );
        this.user = new User(1, 100);
    }

    /**
     * Тест на создание бюджета без существующих операций
     */
    @Test
    public void currentMonthNoOperations() {
        TestYearMonth currentYM = new TestYearMonth();
        CommandData command = new CommandData(this.mockBot, this.user,
                "budget_create", List.of(currentYM.getDotFormat(), "100000", "90000"));

        transactionManager.produceTransaction(session -> this.botHandler.handleCommand(command, session));

        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        MockMessage message = this.mockBot.poolMessageQueue();
        Assert.assertEquals("""
                        Бюджет на %s %d создан.
                        Ожидаемые доходы: 100 000
                        Ожидаемые расходы: 90 000
                        Текущие доходы: 0
                        Текущие расходы: 0
                        Текущий баланс: 100
                        Нужно еще заработать: 100 000
                        Еще осталось на траты: 90 000""".formatted(currentYM.getMonthName(), currentYM.getYear()),
                message.text());
        Assert.assertEquals(this.user, message.receiver());
    }

    /**
     * Тест на создание бюджета с существующими операциями
     */
    @Test
    public void currentMonthAndOperations() {

        TestYearMonth currentYM = new TestYearMonth();
        Category fakeIncomeCategory = new Category(this.user, "Fake Income", CategoryType.INCOME);
        Category fakeExpenseCategory = new Category(this.user, "Fake Expenses", CategoryType.EXPENSE);

        transactionManager.produceTransaction(session -> {
            this.operationRepository.addOperation(session, this.user, fakeIncomeCategory, 7000);
            this.operationRepository.addOperation(session, this.user, fakeExpenseCategory, 6000);
            CommandData command = new CommandData(this.mockBot, this.user,
                    "budget_create", List.of(currentYM.getDotFormat(), "100000", "90000"));
            this.botHandler.handleCommand(command, session);
        });

        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        MockMessage message = this.mockBot.poolMessageQueue();
        Assert.assertEquals("""
                        Бюджет на %s %d создан.
                        Ожидаемые доходы: 100 000
                        Ожидаемые расходы: 90 000
                        Текущие доходы: 7 000
                        Текущие расходы: 6 000
                        Текущий баланс: 100
                        Нужно еще заработать: 93 000
                        Еще осталось на траты: 84 000""".formatted(currentYM.getMonthName(), currentYM.getYear()),
                message.text());
    }

    /**
     * Тест на создание бюджета на прошлый месяц (должна вылезти ошибка)
     */
    @Test
    public void oldMonth() {
        TestYearMonth oldYM = new TestYearMonth().minusMonths(1);
        CommandData command = new CommandData(this.mockBot, this.user,
                "budget_create", List.of(oldYM.getDotFormat(), "100000", "90000"));

        transactionManager.produceTransaction(session -> this.botHandler.handleCommand(command, session));

        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        MockMessage message = this.mockBot.poolMessageQueue();
        Assert.assertEquals("Вы не можете создавать бюджеты за прошедшие месяцы!", message.text());
        Assert.assertEquals(this.user, message.receiver());
    }

    /**
     * Тест на создание бюджета на будущий месяц
     */
    @Test
    public void futureMonth() {
        TestYearMonth futureYM = new TestYearMonth().plusMonths(1);
        CommandData command = new CommandData(this.mockBot, this.user,
                "budget_create", List.of(futureYM.getDotFormat(), "100000", "90000"));

        transactionManager.produceTransaction(session -> this.botHandler.handleCommand(command, session));


        Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
        MockMessage message = this.mockBot.poolMessageQueue();
        Assert.assertEquals("""
                        Бюджет на %s %d создан.
                        Ожидаемые доходы: 100 000
                        Ожидаемые расходы: 90 000
                        Текущие доходы: 0
                        Текущие расходы: 0
                        Текущий баланс: 100
                        Нужно еще заработать: 100 000
                        Еще осталось на траты: 90 000""".formatted(futureYM.getMonthName(), futureYM.getYear()),
                message.text());
    }

    /**
     * Тест на вывоз команды с неправильной датой
     */
    @Test
    public void wrongDateArg() {
        String[] wrongDates = new String[]{"1.2023", "01.23", ".2023", ".23", "0.23", "2023", "01", "1", "_"};
        transactionManager.produceTransaction(session -> {
            for (String wrongDate : wrongDates) {
                CommandData command = new CommandData(this.mockBot, this.user,
                        "budget_create", List.of(wrongDate, "100000", "90000"));
                this.botHandler.handleCommand(command, session);
                Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
                MockMessage message = this.mockBot.poolMessageQueue();
                Assert.assertEquals("Дата введена неверно! Введите ее в формате [mm.yyyy - месяц.год]",
                        message.text());
                Assert.assertEquals(this.user, message.receiver());
            }
        });
    }

    /**
     * Тест на вывоз команды с неправильным числом
     */
    @Test
    public void wrongAmountArgs() {
        String[] wrongAmountArgs = new String[]{"0", "-100", "NaN"};
        TestYearMonth currentYM = new TestYearMonth();
        transactionManager.produceTransaction(session -> {
            for (CategoryType type : CategoryType.values()) {
                for (String wrongAmount : wrongAmountArgs) {
                    String incomeAmount = (type == CategoryType.INCOME) ? wrongAmount : "100000";
                    String expenseAmount = (type == CategoryType.EXPENSE) ? wrongAmount : "90000";
                    CommandData command = new CommandData(this.mockBot, this.user,
                            "budget_create", List.of(currentYM.getDotFormat(), incomeAmount, expenseAmount));
                    this.botHandler.handleCommand(command, session);

                    Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
                    MockMessage message = this.mockBot.poolMessageQueue();
                    Assert.assertEquals("Все суммы должны быть больше нуля!", message.text());
                    Assert.assertEquals(this.user, message.receiver());
                }
            }
        });
    }

    /**
     * Тест на вывоз команды с полностью неправильными аргументами
     */
    @Test
    public void wrongEntireArgs() {
        String currentDateArg = new TestYearMonth().getDotFormat();
        List<List<String>> wrongArgsCases = List.of(
                List.of(),
                List.of(currentDateArg),
                List.of(currentDateArg, "1"),
                List.of(currentDateArg, "1", "1", "1")
        );

        transactionManager.produceTransaction(session -> {
            for (List<String> wrongArgs : wrongArgsCases) {
                CommandData command = new CommandData(this.mockBot, this.user,
                        "budget_create", wrongArgs);
                this.botHandler.handleCommand(command, session);

                Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
                MockMessage message = this.mockBot.poolMessageQueue();
                Assert.assertEquals("Неверно введена команда! Введите " +
                        "/budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы]", message.text());
                Assert.assertEquals(this.user, message.receiver());
            }
        });
    }
}
