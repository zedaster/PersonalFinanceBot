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
import ru.naumen.personalfinancebot.model.*;
import ru.naumen.personalfinancebot.repository.ClearQueryManager;
import ru.naumen.personalfinancebot.repository.TransactionManager;
import ru.naumen.personalfinancebot.repository.budget.HibernateBudgetRepository;
import ru.naumen.personalfinancebot.repository.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingStandardCategoryException;
import ru.naumen.personalfinancebot.repository.category.exception.ExistingUserCategoryException;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.service.InputDateFormatService;
import ru.naumen.personalfinancebot.service.OutputMonthFormatService;

import java.time.YearMonth;
import java.util.List;

/**
 * Тесты для команды "/budget_create".
 */
public class CreateBudgetTest {
    /**
     * Сервис для форматирования дат, которые передаются в команду
     */
    private final InputDateFormatService inputFormatter = new InputDateFormatService();

    /**
     * Сервис для форматирования названия месяца, которое ожидается на выходе
     */
    private final OutputMonthFormatService monthFormatter = new OutputMonthFormatService();

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
    private final HibernateUserRepository userRepository;

    /**
     * Хранилище категорий
     */
    private final HibernateCategoryRepository categoryRepository;

    /**
     * Хранилище бюджетов
     */
    private final HibernateBudgetRepository budgetRepository;

    /**
     * Хранилище опреаций
     */
    private final HibernateOperationRepository operationRepository;

    /**
     * Экземпляр класс фейковой реализации бота
     */
    private MockBot mockBot;

    /**
     * Пользователь
     */
    private User user;

    public CreateBudgetTest() {
        SessionFactory sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.transactionManager = new TransactionManager(sessionFactory);
        this.userRepository = new HibernateUserRepository();
        this.categoryRepository = new HibernateCategoryRepository();
        this.operationRepository = new HibernateOperationRepository();
        this.budgetRepository = new HibernateBudgetRepository();
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
    }

    /**
     * Очистка репозиториев после каждого теста
     */
    @After
    public void clearRepositories() {
        this.transactionManager.produceTransaction(session -> {
            new ClearQueryManager().clear(session, Budget.class, Operation.class, Category.class, User.class);
        });
    }

    /**
     * Тест на создание бюджета без существующих операций
     */
    @Test
    public void currentMonthNoOperations() {
        YearMonth currentYM = YearMonth.now();
        CommandData command = new CommandData(this.mockBot, this.user,
                "budget_create", List.of(inputFormatter.formatYearMonth(currentYM), "100000", "90000"));

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
                        Еще осталось на траты: 90 000""".formatted(
                        monthFormatter.formatRuMonthName(currentYM.getMonth()),
                        currentYM.getYear()),
                message.text());
        Assert.assertEquals(this.user, message.receiver());
    }

    /**
     * Тест на создание бюджета с существующими операциями
     */
    @Test
    public void currentMonthAndOperations() {
        YearMonth currentYM = YearMonth.now();
        transactionManager.produceTransaction(session -> {
            Category fakeIncomeCategory;
            Category fakeExpenseCategory;
            try {
                fakeIncomeCategory = this.categoryRepository
                        .createUserCategory(session, this.user, CategoryType.INCOME, "Fake Income");
                fakeExpenseCategory = this.categoryRepository
                        .createUserCategory(session, this.user, CategoryType.EXPENSE, "Fake Expenses");

            } catch (ExistingStandardCategoryException | ExistingUserCategoryException e) {
                throw new RuntimeException(e);
            }
            this.operationRepository.addOperation(session, this.user, fakeIncomeCategory, 7000);
            this.operationRepository.addOperation(session, this.user, fakeExpenseCategory, 6000);
            CommandData command = new CommandData(this.mockBot, this.user,
                    "budget_create", List.of(inputFormatter.formatYearMonth(currentYM), "100000", "90000"));
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
                        Еще осталось на траты: 84 000""".formatted(
                        monthFormatter.formatRuMonthName(currentYM.getMonth()),
                        currentYM.getYear()),
                message.text());
    }

    /**
     * Тест на создание бюджета на прошлый месяц (должна вылезти ошибка)
     */
    @Test
    public void oldMonth() {
        YearMonth oldYM = YearMonth.now().minusMonths(1);
        CommandData command = new CommandData(this.mockBot, this.user,
                "budget_create", List.of(inputFormatter.formatYearMonth(oldYM), "100000", "90000"));

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
        YearMonth futureYM = YearMonth.now().plusMonths(1);
        CommandData command = new CommandData(this.mockBot, this.user,
                "budget_create", List.of(inputFormatter.formatYearMonth(futureYM), "100000", "90000"));

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
                        Еще осталось на траты: 90 000""".formatted(
                        monthFormatter.formatRuMonthName(futureYM.getMonth()),
                        futureYM.getYear()),
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
        YearMonth currentYM = YearMonth.now();
        transactionManager.produceTransaction(session -> {
            for (String wrongAmount : wrongAmountArgs) {
                final String incomeAmount = "100000";
                CommandData commandWrongExpenses = new CommandData(
                        this.mockBot,
                        this.user,
                        "budget_create",
                        List.of(inputFormatter.formatYearMonth(currentYM), incomeAmount, wrongAmount));
                this.botHandler.handleCommand(commandWrongExpenses, session);

                final String expenseAmount = "90000";
                CommandData commandWrongIncome = new CommandData(
                        this.mockBot,
                        this.user,
                        "budget_create",
                        List.of(inputFormatter.formatYearMonth(currentYM), wrongAmount, expenseAmount));
                this.botHandler.handleCommand(commandWrongIncome, session);
            }

            Assert.assertEquals(6, this.mockBot.getMessageQueueSize());
            for (int i = 0; i < 6; i++) {
                MockMessage message = this.mockBot.poolMessageQueue();
                Assert.assertEquals("Все суммы должны быть больше нуля!", message.text());
                Assert.assertEquals(this.user, message.receiver());
            }

        });
    }

    /**
     * Тест на вызов команды с полностью неправильными аргументами
     */
    @Test
    public void wrongEntireArgs() {
        String currentDateArg = inputFormatter.formatYearMonth(YearMonth.now());
        List<List<String>> wrongArgsCases = List.of(
                List.of(),
                List.of(currentDateArg),
                List.of(currentDateArg, "1"),
                List.of(currentDateArg, "1", "1", "1"));

        transactionManager.produceTransaction(session -> {
            for (List<String> wrongArgs : wrongArgsCases) {
                CommandData command = new CommandData(this.mockBot, this.user,
                        "budget_create", wrongArgs);
                this.botHandler.handleCommand(command, session);

                Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
                MockMessage message = this.mockBot.poolMessageQueue();
                Assert.assertEquals("Неверно введена команда! Введите " +
                                    "/budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы]",
                        message.text());
                Assert.assertEquals(this.user, message.receiver());
            }
        });
    }
}
