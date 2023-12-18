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
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.ClearQueryManager;
import ru.naumen.personalfinancebot.repository.TransactionManager;
import ru.naumen.personalfinancebot.repository.budget.HibernateBudgetRepository;
import ru.naumen.personalfinancebot.repository.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;

import java.util.List;

/**
 * Тесты для команд "/budget_set_{"expenses" / "income"}"
 */
public class EditBudgetTest {
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

    public EditBudgetTest() {
        SessionFactory sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.transactionManager = new TransactionManager(sessionFactory);
        this.userRepository = new HibernateUserRepository();
        this.categoryRepository = new HibernateCategoryRepository();
        this.budgetRepository = new HibernateBudgetRepository();
        this.operationRepository = new HibernateOperationRepository();
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
     * Тест на редактирование планируемых трат бюджета в текущем месяце
     */
    @Test
    public void editExpensesCurrentBudget() {
        transactionManager.produceTransaction(session -> {
            TestYearMonth currentYM = new TestYearMonth();
            this.budgetRepository.saveBudget(session, new Budget(this.user, 100_000, 90_000, currentYM.getYearMonth()));
            CommandData command = new CommandData(this.mockBot, this.user,
                    "budget_set_expenses", List.of(currentYM.getDotFormat(), "120000"));
            this.botHandler.handleCommand(command, session);

            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("""
                    Бюджет на %s %d изменен:
                    Ожидаемые доходы: 100 000
                    Ожидаемые расходы: 120 000""".formatted(
                    currentYM.getMonthName(),
                    currentYM.getYear()), message.text());
            Assert.assertEquals(this.user, message.receiver());
        });
    }

    /**
     * Тест на редактирование планируемых доходов бюджета в текущем месяце
     */
    @Test
    public void editIncomeCurrentBudget() {
        transactionManager.produceTransaction(session -> {
            TestYearMonth currentYM = new TestYearMonth();
            this.budgetRepository.saveBudget(session, new Budget(this.user, 100_000, 90_000, currentYM.getYearMonth()));
            CommandData command = new CommandData(this.mockBot, this.user,
                    "budget_set_income", List.of(currentYM.getDotFormat(), "150000"));
            this.botHandler.handleCommand(command, session);

            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("""
                    Бюджет на %s %d изменен:
                    Ожидаемые доходы: 150 000
                    Ожидаемые расходы: 90 000""".formatted(
                    currentYM.getMonthName(),
                    currentYM.getYear()), message.text());
        });
    }

    /**
     * Тест на редактирование бюджета в одном из будущих месяцев
     */
    @Test
    public void editFutureBudget() {
        transactionManager.produceTransaction(session -> {
            TestYearMonth futureYM = new TestYearMonth().plusMonths(1);
            this.budgetRepository.saveBudget(session, new Budget(this.user, 100_000, 90_000, futureYM.getYearMonth()));
            CommandData command = new CommandData(this.mockBot, this.user,
                    "budget_set_income", List.of(futureYM.getDotFormat(), "200000"));
            this.botHandler.handleCommand(command, session);

            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("""
                    Бюджет на %s %d изменен:
                    Ожидаемые доходы: 200 000
                    Ожидаемые расходы: 90 000""".formatted(
                    futureYM.getMonthName(),
                    futureYM.getYear()), message.text());
        });
    }

    /**
     * Тест на редактирование бюджета в одном из прошлых месяцев (должна выйти ошибка)
     */
    @Test
    public void editOldBudget() {
        transactionManager.produceTransaction(session -> {
            TestYearMonth futureYM = new TestYearMonth().minusMonths(1);
            this.budgetRepository.saveBudget(session, new Budget(this.user, 100_000, 90_000, futureYM.getYearMonth()));
            CommandData command = new CommandData(this.mockBot, this.user,
                    "budget_set_income", List.of(futureYM.getDotFormat(), "200000"));
            this.botHandler.handleCommand(command, session);

            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("Вы не можете изменять бюджеты за прошедшие месяцы!", message.text());
            Assert.assertEquals(this.user, message.receiver());
        });
    }

    /**
     * Тест на редактирование несуществующего бюджета (должна выйти ошибка)
     */
    @Test
    public void editNonExistentBudget() {
        transactionManager.produceTransaction(session -> {
            TestYearMonth currentYM = new TestYearMonth();
            CommandData command = new CommandData(this.mockBot, this.user,
                    "budget_set_expenses", List.of(currentYM.getDotFormat(), "1000"));
            this.botHandler.handleCommand(command, session);

            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("Бюджет на этот период не найден! Создайте его командой " +
                    "/budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы]", message.text());
            Assert.assertEquals(this.user, message.receiver());
        });
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
                        "budget_set_expenses", List.of(wrongDate, "100000"));
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
            for (String wrongAmount : wrongAmountArgs) {
                CommandData command = new CommandData(this.mockBot, this.user,
                        "budget_set_expenses", List.of(currentYM.getDotFormat(), wrongAmount));
                this.botHandler.handleCommand(command, session);

                Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
                MockMessage message = this.mockBot.poolMessageQueue();
                Assert.assertEquals("Все суммы должны быть больше нуля!", message.text());
                Assert.assertEquals(this.user, message.receiver());
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
                List.of(currentDateArg, "1", "1", "1")
        );

        transactionManager.produceTransaction(session -> {
            for (List<String> wrongArgs : wrongArgsCases) {
                CommandData command = new CommandData(this.mockBot, this.user,
                        "budget_set_expenses", wrongArgs);
                this.botHandler.handleCommand(command, session);

                Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
                MockMessage message = this.mockBot.poolMessageQueue();
                Assert.assertEquals("Неверно введена команда! Введите " +
                        "/budget_set_[income/expenses] [mm.yyyy - месяц.год] [ожидаемый доход/расход]", message.text());
                Assert.assertEquals(this.user, message.receiver());
            }
        });
    }
}
