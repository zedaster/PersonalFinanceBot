package ru.naumen.personalfinancebot.handler.command.budget;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.FinanceBotHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.ClearQueryManager;
import ru.naumen.personalfinancebot.repository.TransactionManager;
import ru.naumen.personalfinancebot.repository.budget.HibernateBudgetRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.service.InputDateFormatService;
import ru.naumen.personalfinancebot.service.OutputMonthFormatService;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для обработки команды "/budget_list"
 */
public class ListBudgetTest {
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
     * Хранилище бюджетов
     */
    private final HibernateBudgetRepository budgetRepository;

    /**
     * Моковое хранилище опреаций
     */
    private final OperationRepository operationRepository;

    /**
     * Экземпляр класс фейковой реализации бота
     */
    private MockBot mockBot;

    /**
     * Пользователь
     */
    private User user;

    public ListBudgetTest() {
        SessionFactory sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.transactionManager = new TransactionManager(sessionFactory);
        this.userRepository = new HibernateUserRepository();
        this.budgetRepository = new HibernateBudgetRepository();
        this.operationRepository = Mockito.mock(OperationRepository.class);
        this.botHandler = new FinanceBotHandler(
                this.userRepository,
                this.operationRepository,
                null,
                this.budgetRepository
        );
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
            new ClearQueryManager().clear(session, Budget.class, User.class);
        });
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
        YearMonth currentYM = YearMonth.now();
        YearMonth minusOneMonthYM = currentYM.minusMonths(1);

        Mockito.doReturn(9000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                Mockito.any(Session.class),
                Mockito.any(User.class),
                Mockito.eq(CategoryType.INCOME),
                Mockito.eq(minusOneMonthYM)
        );
        Mockito.doReturn(8000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                Mockito.any(Session.class),
                Mockito.any(User.class),
                Mockito.eq(CategoryType.EXPENSE),
                Mockito.eq(minusOneMonthYM)
        );

        Mockito.doReturn(7000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                Mockito.any(Session.class),
                Mockito.any(User.class),
                Mockito.eq(CategoryType.INCOME),
                Mockito.eq(currentYM)
        );
        Mockito.doReturn(6000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                Mockito.any(Session.class),
                Mockito.any(User.class),
                Mockito.eq(CategoryType.EXPENSE),
                Mockito.eq(currentYM)
        );

        transactionManager.produceTransaction(session -> {
            this.budgetRepository.saveBudget(session, new Budget(user, 100_000, 90_000, minusOneMonthYM));
            this.budgetRepository.saveBudget(session, new Budget(user, 80_000, 70_000, currentYM));

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
                            .formatted(monthFormatter.formatRuMonthName(minusOneMonthYM.getMonth()), minusOneMonthYM.getYear(), monthFormatter.formatRuMonthName(currentYM.getMonth()),
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
            for (int i = 13; i >= 0; i--) {
                YearMonth testYM = YearMonth.now().minusMonths(i);
                this.budgetRepository.saveBudget(session, new Budget(user, 100_000, 90_000, testYM));
                Mockito.doReturn(9000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                        Mockito.eq(session),
                        Mockito.eq(user),
                        Mockito.eq(CategoryType.INCOME),
                        Mockito.eq(testYM)
                );
                Mockito.doReturn(8000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                        Mockito.eq(session),
                        Mockito.eq(user),
                        Mockito.eq(CategoryType.EXPENSE),
                        Mockito.eq(testYM)
                );
                if (i != 13) {
                    argsToReplace.add(monthFormatter.formatRuMonthName(testYM.getMonth()));
                    argsToReplace.add(String.valueOf(testYM.getYear()));
                }
            }
            expectResponse = expectResponse.formatted(argsToReplace.toArray());

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
            YearMonth ymJan2022 = YearMonth.of(2022, 1);
            YearMonth ymFeb2022 = YearMonth.of(2022, 2);

            this.budgetRepository.saveBudget(session, new Budget(user, 100_000, 90_000, ymJan2022));
            this.budgetRepository.saveBudget(session, new Budget(user, 80_000, 70_000, ymFeb2022));

            Mockito.doReturn(9000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                    Mockito.eq(session),
                    Mockito.eq(user),
                    Mockito.eq(CategoryType.INCOME),
                    Mockito.eq(ymJan2022)
            );
            Mockito.doReturn(8000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                    Mockito.eq(session),
                    Mockito.eq(user),
                    Mockito.eq(CategoryType.EXPENSE),
                    Mockito.eq(ymJan2022)
            );
            Mockito.doReturn(7000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                    Mockito.eq(session),
                    Mockito.eq(user),
                    Mockito.eq(CategoryType.INCOME),
                    Mockito.eq(ymFeb2022)
            );
            Mockito.doReturn(6000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                    Mockito.eq(session),
                    Mockito.eq(user),
                    Mockito.eq(CategoryType.EXPENSE),
                    Mockito.eq(ymFeb2022)
            );

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
        YearMonth nov22ym = YearMonth.of(2022, 11);
        transactionManager.produceTransaction(session -> {
            for (int i = 0; i < 4; i++) {
                YearMonth testYM = nov22ym.plusMonths(i);
                this.budgetRepository.saveBudget(session, new Budget(user, 100_000, 90_000, testYM));
                Mockito.doReturn(9000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                        Mockito.eq(session),
                        Mockito.eq(user),
                        Mockito.eq(CategoryType.INCOME),
                        Mockito.eq(testYM)
                );
                Mockito.doReturn(8000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                        Mockito.eq(session),
                        Mockito.eq(user),
                        Mockito.eq(CategoryType.EXPENSE),
                        Mockito.eq(testYM)
                );
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
            YearMonth testYM = YearMonth.of(2022, 12);
            this.budgetRepository.saveBudget(session, new Budget(user, 100_000, 90_000, testYM));

            Mockito.doReturn(101_000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                    Mockito.eq(session),
                    Mockito.eq(user),
                    Mockito.eq(CategoryType.INCOME),
                    Mockito.eq(testYM)
            );
            Mockito.doReturn(91_000d).when(this.operationRepository).getCurrentUserPaymentSummary(
                    Mockito.eq(session),
                    Mockito.eq(user),
                    Mockito.eq(CategoryType.EXPENSE),
                    Mockito.eq(testYM)
            );

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
        String currentDateArg = inputFormatter.formatYearMonth(YearMonth.now());
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
