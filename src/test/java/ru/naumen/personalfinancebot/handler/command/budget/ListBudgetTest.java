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

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для обработки команды "/budget_list"
 */
public class ListBudgetTest {
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
    private FakeOperationRepository operationRepository;

    /**
     * Пользователь
     */
    private User user;

    public ListBudgetTest() {
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
                this.budgetRepository
        );
        this.user = new User(1, 100);
    }

    /**
     * Тест команды без аргументов и без бюджетов в БД
     */
    @Test
    public void noArgsNoBudgets() {
        transactionManager.produceTransaction(session -> {
            CommandData command = new CommandData(this.mockBot, this.user, "budget_list", List.of());
            this.botHandler.handleCommand(command, session);
            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("У вас не было бюджетов за этот период. Для создания бюджета введите " +
                                "/budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы]", message.text());
            Assert.assertEquals(this.user, message.receiver());
        });
    }

    /**
     * Тест команды без аргументов и с бюджетами на 2 месяца в БД
     */
    @Test
    public void noArgsSomeMonths() {
        transactionManager.produceTransaction(session -> {
            Category fakeIncome = new Category(user, "Fake Income", CategoryType.INCOME);
            Category fakeExpense = new Category(user, "Fake Expense", CategoryType.EXPENSE);
            TestYearMonth currentYM = new TestYearMonth();
            TestYearMonth minusOneMonthYM = currentYM.minusMonths(1);
            this.budgetRepository.saveBudget(session, new Budget(user, 100_000, 90_000, minusOneMonthYM.getYearMonth()));
            this.budgetRepository.saveBudget(session, new Budget(user, 80_000, 70_000, currentYM.getYearMonth()));
            this.operationRepository.addOperation(session, user, fakeIncome, 9000, minusOneMonthYM.atDay(1));
            this.operationRepository.addOperation(session, user, fakeExpense, 8000, minusOneMonthYM.atDay(1));
            this.operationRepository.addOperation(session, user, fakeIncome, 7000, currentYM.atDay(1));
            this.operationRepository.addOperation(session, user, fakeExpense, 6000, currentYM.atDay(1));

            CommandData command = new CommandData(this.mockBot, this.user, "budget_list", List.of());
            this.botHandler.handleCommand(command, session);
            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("""
                            Ваши запланированные доходы и расходы по месяцам:
                            %s %d:
                            Ожидание: + 100 000 | - 90 000
                            Реальность: + 9 000 | - 8 000
                                                    
                            %s %d:
                            Ожидание: + 80 000 | - 70 000
                            Реальность: + 7 000 | - 6 000
                                            
                            Данные показаны за последние 12 месяцев. Чтобы посмотреть данные, например, за 2022, введите /budget_list 2022.
                            Для показа данных по определенным месяцам, например, с ноября 2022 по январь 2023 введите /budget_list 10.2022 01.2023"""
                            .formatted(minusOneMonthYM.getMonthName(), minusOneMonthYM.getYear(), currentYM.getMonthName(),
                                    currentYM.getYear()),
                    message.text());
            Assert.assertEquals(this.user, message.receiver());
        });
    }

    /**
     * Тест команды без аргументов и с бюджетами на 13 месяцев в БД (должно быть выведено только 12 за текущий и
     * предыдущие месяцы)
     */
    @Test
    public void noArgsTwelveOfThirteenMonths() {
        transactionManager.produceTransaction(session -> {
            Category fakeIncome = new Category(user, "Fake Income", CategoryType.INCOME);
            Category fakeExpense = new Category(user, "Fake Expense", CategoryType.EXPENSE);

            String expectResponse = """
                    Ваши запланированные доходы и расходы по месяцам:
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    %s %s:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 9 000 | - 8 000
                                        
                    Данные показаны за последние 12 месяцев. Чтобы посмотреть данные, например, за 2022, введите /budget_list 2022.
                    Для показа данных по определенным месяцам, например, с ноября 2022 по январь 2023 введите /budget_list 10.2022 01.2023""";

            List<String> argsToReplace = new ArrayList<>();

            for (int i = 12; i >= 0; i--) {
                TestYearMonth testYM = new TestYearMonth().minusMonths(i);
                this.budgetRepository.saveBudget(session, new Budget(user, 100_000, 90_000, testYM.getYearMonth()));
                this.operationRepository.addOperation(session, user, fakeIncome, 9000, testYM.atDay(1));
                this.operationRepository.addOperation(session, user, fakeExpense, 8000, testYM.atDay(1));
                argsToReplace.add(testYM.getMonthName());
                argsToReplace.add(String.valueOf(testYM.getYear()));
            }

            expectResponse = expectResponse.formatted(argsToReplace.toArray());

            // Добавляем 13 месяц, который не нужно выводить
            TestYearMonth testYM = new TestYearMonth().minusMonths(13);
            this.budgetRepository.saveBudget(session, new Budget(user, 100_000, 90_000, testYM.getYearMonth()));
            this.operationRepository.addOperation(session, user, fakeIncome, 9000, testYM.atDay(1));
            this.operationRepository.addOperation(session, user, fakeExpense, 8000, testYM.atDay(1));

            CommandData command = new CommandData(this.mockBot, this.user, "budget_list", List.of());
            this.botHandler.handleCommand(command, session);
            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals(expectResponse, message.text());
        });
    }

    /**
     * Тест на вывод бюджетов за определенный год при наличии бюджетов в хранилище
     */
    @Test
    public void certainYear() {
        transactionManager.produceTransaction(session -> {
            Category fakeIncome = new Category(user, "Fake Income", CategoryType.INCOME);
            Category fakeExpense = new Category(user, "Fake Expense", CategoryType.EXPENSE);
            TestYearMonth ymJan2022 = new TestYearMonth(YearMonth.of(2022, 1));
            TestYearMonth ymFeb2022 = new TestYearMonth(YearMonth.of(2022, 2));
            this.budgetRepository.saveBudget(session, new Budget(user, 100_000, 90_000, ymJan2022.getYearMonth()));
            this.budgetRepository.saveBudget(session, new Budget(user, 80_000, 70_000, ymFeb2022.getYearMonth()));
            this.operationRepository.addOperation(session, user, fakeIncome, 9000, ymJan2022.atDay(1));
            this.operationRepository.addOperation(session, user, fakeExpense, 8000, ymJan2022.atDay(1));
            this.operationRepository.addOperation(session, user, fakeIncome, 7000, ymFeb2022.atDay(1));
            this.operationRepository.addOperation(session, user, fakeExpense, 6000, ymFeb2022.atDay(1));

            CommandData command = new CommandData(this.mockBot, this.user, "budget_list", List.of("2022"));
            this.botHandler.handleCommand(command, session);
            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("""
                            Ваши запланированные доходы и расходы по месяцам:
                            Январь 2022:
                            Ожидание: + 100 000 | - 90 000
                            Реальность: + 9 000 | - 8 000
                                                    
                            Февраль 2022:
                            Ожидание: + 80 000 | - 70 000
                            Реальность: + 7 000 | - 6 000
                                            
                            Данные показаны за 2022 год.""",
                    message.text());
        });
    }

    /**
     * Тест на вывод бюджетов в определенном промежутке дат
     */
    @Test
    public void yearMonthRange() {
        Category fakeIncome = new Category(user, "Fake Income", CategoryType.INCOME);
        Category fakeExpense = new Category(user, "Fake Expense", CategoryType.EXPENSE);

        String expectResponse = """
                Ваши запланированные доходы и расходы по месяцам:
                Ноябрь 2022:
                Ожидание: + 100 000 | - 90 000
                Реальность: + 9 000 | - 8 000
                                
                Декабрь 2022:
                Ожидание: + 100 000 | - 90 000
                Реальность: + 9 000 | - 8 000
                                
                Январь 2023:
                Ожидание: + 100 000 | - 90 000
                Реальность: + 9 000 | - 8 000
                                
                Февраль 2023:
                Ожидание: + 100 000 | - 90 000
                Реальность: + 9 000 | - 8 000
                                
                Данные показаны за 4 месяц(-ев).""";
        TestYearMonth nov22ym = new TestYearMonth(YearMonth.of(2022, 11));
        transactionManager.produceTransaction(session -> {
            for (int i = 0; i < 4; i++) {
                TestYearMonth testYM = nov22ym.plusMonths(i);
                this.budgetRepository.saveBudget(session, new Budget(user, 100_000, 90_000, testYM.getYearMonth()));
                this.operationRepository.addOperation(session, user, fakeIncome, 9000, testYM.atDay(1));
                this.operationRepository.addOperation(session, user, fakeExpense, 8000, testYM.atDay(1));
            }

            CommandData command = new CommandData(this.mockBot, this.user, "budget_list",
                    List.of("11.2022", "02.2023"));
            this.botHandler.handleCommand(command, session);
            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals(expectResponse, message.text());
        });

    }

    /**
     * Тест на вывод бюджетов за определенный год при отсутствии бюджетов в хранилище
     */
    @Test
    public void emptyYear() {
        transactionManager.produceTransaction(session -> {
            CommandData command = new CommandData(this.mockBot, this.user, "budget_list", List.of("2022"));
            this.botHandler.handleCommand(command, session);
            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("У вас не было бюджетов за этот период. Для создания бюджета введите " +
                                "/budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы]", message.text());
            Assert.assertEquals(this.user, message.receiver());
        });
    }

    /**
     * Тест на некорректные даты в промежутке
     */
    @Test
    public void incorrectYearMonthRange() {
        List<List<String>> wrongArgsCases = List.of(
                List.of("10.22", "01.2023"),
                List.of("10.2022", "01.23"),
                List.of("0.2022", "01.2023"),
                List.of(".2022", "01.2023")
        );
        transactionManager.produceTransaction(session -> {
            for (List<String> wrongArgs : wrongArgsCases) {
                CommandData command = new CommandData(this.mockBot, this.user, "budget_list",
                        wrongArgs);
                this.botHandler.handleCommand(command, session);
                Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
                MockMessage message = this.mockBot.poolMessageQueue();
                Assert.assertEquals("Дата введена неверно! Введите ее в формате [mm.yyyy - месяц.год]", message.text());
                Assert.assertEquals(this.user, message.receiver());
            }
        });
    }

    /**
     * Тест на такой промежуток, где дата начала больше даты конца
     */
    @Test
    public void yearMonthRangeFromAfterTo() {
        transactionManager.produceTransaction(session -> {
            CommandData command = new CommandData(this.mockBot, this.user, "budget_list",
                    List.of("12.2022", "11.2022"));
            this.botHandler.handleCommand(command, session);
            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("Дата начала не может быть позднее даты конца периода!", message.text());
            Assert.assertEquals(this.user, message.receiver());
        });
    }

    /**
     * Запрос бюджетов в промежутке от одного месяца до того же самого, в котором реальные доходы и расходы превысили
     * ожидаемые.
     */
    @Test
    public void oneMonthRangeAndRealOperationsExceededExpected() {
        transactionManager.produceTransaction(session -> {
            Category fakeIncome = new Category(user, "Fake Income", CategoryType.INCOME);
            Category fakeExpense = new Category(user, "Fake Expense", CategoryType.EXPENSE);

            TestYearMonth testYM = new TestYearMonth(YearMonth.of(2022, 12));
            this.budgetRepository.saveBudget(session, new Budget(user, 100_000, 90_000, testYM.getYearMonth()));
            this.operationRepository.addOperation(session, user, fakeIncome, 101_000, testYM.atDay(1));
            this.operationRepository.addOperation(session, user, fakeExpense, 91_000, testYM.atDay(1));

            CommandData command = new CommandData(this.mockBot, this.user, "budget_list",
                    List.of("12.2022", "12.2022"));
            this.botHandler.handleCommand(command, session);
            Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
            MockMessage message = this.mockBot.poolMessageQueue();
            Assert.assertEquals("""
                    Ваши запланированные доходы и расходы по месяцам:
                    Декабрь 2022:
                    Ожидание: + 100 000 | - 90 000
                    Реальность: + 101 000 | - 91 000
                                    
                    Данные показаны за 1 месяц(-ев).""", message.text());
        });
    }

    /**
     * Тест с полностью некорректными аргументами
     */
    @Test
    public void wrongEntireArgs() {
        String currentDateArg = new TestYearMonth().getDotFormat();
        List<List<String>> wrongArgsCases = List.of(
                List.of(currentDateArg, "1", "1", "1")
        );
        transactionManager.produceTransaction(session -> {
            for (List<String> wrongArgs : wrongArgsCases) {
                CommandData command = new CommandData(this.mockBot, this.user,
                        "budget_list", wrongArgs);
                this.botHandler.handleCommand(command, session);

                Assert.assertEquals(1, this.mockBot.getMessageQueueSize());
                MockMessage message = this.mockBot.poolMessageQueue();
                Assert.assertEquals("""
                                Неверно введена команда! Введите
                                или /budget_list - вывод бюджетов за 12 месяцев (текущий + предыдущие),
                                или /budget_list [год] - вывод бюджетов за определенный год,
                                или /budget_list [mm.yyyy - месяц.год] [mm.yyyy - месяц.год] - вывод бюджетов за указанный промежуток.""",
                        message.text());
                Assert.assertEquals(this.user, message.receiver());
            }
        });
    }
}
