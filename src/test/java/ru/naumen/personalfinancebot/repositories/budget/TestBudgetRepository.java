package ru.naumen.personalfinancebot.repositories.budget;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Random;

/**
 * Класс для тестирования {@link HibernateBudgetRepository}
 *
 * @author aleksandr
 */
public class TestBudgetRepository {
    /**
     * Session factory для работы с сессиями в хранилищах
     */
    private final static SessionFactory sessionFactory;

    /**
     * Репозиторий модели данных {@link Budget}
     */
    private final static BudgetRepository budgetRepository;

    /**
     * Репозиторий модели данных {@link User}
     */
    private final static UserRepository userRepository;


    static {
        sessionFactory = new HibernateConfiguration().getSessionFactory();
        budgetRepository = new HibernateBudgetRepository(sessionFactory);
        userRepository = new HibernateUserRepository(sessionFactory);
    }

    /**
     * Создает пользователя в Базе данных перед запуском тестов
     */
    @BeforeClass
    public static void createUser() {
        User user = new User(1, 1);
        userRepository.saveUser(user);
    }

    /**
     * Тестирует сохранению и получения записи бюджета.
     */
    @Test
    public void saveAndGetBudget() {
        Budget budget = new Budget();
        double expense = 50_000, income = 70_000;
        budget.setExpectedSummary(CategoryType.EXPENSE, expense);
        budget.setExpectedSummary(CategoryType.INCOME, income);
        User user = userRepository.getUserByTelegramChatId(1L).get();
        budget.setUser(user);
        budget.setTargetDate(YearMonth.of(2023, 11));
        budgetRepository.saveBudget(budget);
        Budget budget1 = budgetRepository.getBudget(user, YearMonth.of(2023, 11)).get();

        Assert.assertEquals(budget1.getExpectedSummary(CategoryType.INCOME), budget.getExpectedSummary(CategoryType.INCOME), 0.0);
        Assert.assertEquals(budget1.getExpectedSummary(CategoryType.EXPENSE), budget.getExpectedSummary(CategoryType.EXPENSE), 0.0);

        YearMonth yearMonth = budget1.getTargetDate();

        Assert.assertEquals(2023, yearMonth.getYear(), 0);
        Assert.assertEquals(11, yearMonth.getMonth().getValue(), 0);
    }

    /**
     * Метод сохраняет записи бюджетов для пользователя
     *
     * @param user Пользователь
     */
    private void generateBudgetRange(User user) {
        List<Integer> months = List.of(1, 2, 3, 6, 7, 8, 10, 12);
        for (Integer month : months) {
            budgetRepository.saveBudget(new Budget(user, getRandomPayment(), getRandomPayment(), YearMonth.of(2023, month)));
        }
        budgetRepository.saveBudget(new Budget(user, getRandomPayment(), getRandomPayment(), YearMonth.of(2024, 1)));
    }

    /**
     * Метод проверяет размер полученного диапазона (Списка Бюджетов)
     *
     * @param user            Пользователь
     * @param argumentContext Класс-контекст
     */
    private void assertCorrectListBudgetSize(User user, TestArgumentContext argumentContext) {
        List<Budget> budgets = budgetRepository.selectBudgetRange(user, argumentContext.getFrom(), argumentContext.getTo());
        Assert.assertEquals(argumentContext.getRangeExpectedSize(), budgets.size(), 0);
    }

    /**
     * Метод создаеет разнородные контекстные данные для метода assertCorrectListBudgetSize
     */
    @Test
    public void testSelectBudgetRangeMethod() {
        User user = userRepository.getUserByTelegramChatId(1L).get();
        List<TestArgumentContext> argumentContextList = List.of(
                new TestArgumentContext(YearMonth.of(2023, 1), YearMonth.of(2023, 2), 2),
                new TestArgumentContext(YearMonth.of(2023, 8), YearMonth.of(2023, 10), 2),
                new TestArgumentContext(YearMonth.of(2023, 4), YearMonth.of(2023, 5), 0),
                new TestArgumentContext(YearMonth.of(2023, 1), YearMonth.of(2023, 12), 8),
                new TestArgumentContext(YearMonth.of(2023, 12), YearMonth.of(2024, 1), 2)
        );
        generateBudgetRange(user);
        for (TestArgumentContext argumentContext : argumentContextList) {
            assertCorrectListBudgetSize(user, argumentContext);
        }
    }

    /**
     * Возвращает случайное число
     *
     * @return Случайное число
     */
    private Double getRandomPayment() {
        Random random = new Random();
        return 20_000 + (100_000 - 20_000) * random.nextDouble();
    }

    /**
     * Класс-контекс для тестов
     */
    private class TestArgumentContext {
        /**
         * Начало диапазона
         */
        private final YearMonth from;

        /**
         * Конец диапазона
         */
        private final YearMonth to;

        /**
         * Ожидаемый размер списка
         */
        private final int rangeExpectedSize;

        /**
         * @param from              Начало диапазона
         * @param to                Конец диапазона
         * @param rangeExpectedSize Ожидаемый размер списка
         */
        public TestArgumentContext(YearMonth from, YearMonth to, int rangeExpectedSize) {
            this.from = from;
            this.to = to;
            this.rangeExpectedSize = rangeExpectedSize;
        }

        /**
         * @return Конец диапазона
         */
        public YearMonth getTo() {
            return this.to;
        }

        /**
         * @return Начало диапазона
         */
        public YearMonth getFrom() {
            return this.from;
        }

        /**
         * @return Размер списка
         */
        public int getRangeExpectedSize() {
            return this.rangeExpectedSize;
        }
    }
}
