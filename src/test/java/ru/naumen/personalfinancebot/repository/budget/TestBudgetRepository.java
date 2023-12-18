package ru.naumen.personalfinancebot.repository.budget;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.TransactionManager;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repository.user.UserRepository;

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
     * Репозиторий модели данных {@link Budget}
     */
    private final BudgetRepository budgetRepository;

    /**
     * Репозиторий модели данных {@link User}
     */
    private final UserRepository userRepository;

    /**
     * Менеджер транзакций
     */
    private final TransactionManager transactionManager;

    public TestBudgetRepository() {
        SessionFactory sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.transactionManager = new TransactionManager(sessionFactory);
        this.userRepository = new HibernateUserRepository();
        this.budgetRepository = new HibernateBudgetRepository();
    }

    /**
     * Создает пользователя в Базе данных перед запуском тестов
     */
    public User createUser(long id) {
        User user = new User(id, 100_000);
        this.transactionManager.produceTransaction(session -> {
            userRepository.saveUser(session, user);
        });
        return user;
    }

    /**
     * Тестирует сохранению и получения записи бюджета.
     */
    @Test
    public void saveAndGetBudget() {
        Budget budget = new Budget();
        double expense = 50_000, income = 70_000;
        budget.setExpense(expense);
        budget.setIncome(income);
        User user = this.createUser(1L);
        budget.setUser(user);
        budget.setTargetDate(YearMonth.of(2023, 11));
        transactionManager.produceTransaction(session -> {
            budgetRepository.saveBudget(session, budget);
            Budget budget1 = budgetRepository.getBudget(session, user, YearMonth.of(2023, 11)).get();

            Assert.assertEquals(budget1.getIncome(), budget.getIncome(), 0.0);
            Assert.assertEquals(budget1.getExpense(), budget.getExpense(), 0.0);

            YearMonth yearMonth = budget1.getTargetDate();

            Assert.assertEquals(2023, yearMonth.getYear(), 0);
            Assert.assertEquals(11, yearMonth.getMonth().getValue(), 0);
        });
    }

    /**
     * Метод сохраняет записи бюджетов для пользователя
     *
     * @param user Пользователь
     */
    private void generateBudgetRange(Session session, User user) {
        List<Integer> months = List.of(1, 2, 3, 6, 7, 8, 10, 12);
        for (Integer month : months) {
            budgetRepository.saveBudget(session, new Budget(user, getRandomPayment(), getRandomPayment(), YearMonth.of(2023, month)));
        }
        budgetRepository.saveBudget(session, new Budget(user, getRandomPayment(), getRandomPayment(), YearMonth.of(2024, 1)));
    }

    /**
     * Метод проверяет размер полученного диапазона (Списка Бюджетов)
     *
     * @param user            Пользователь
     * @param argumentContext Класс-контекст
     */
    private void assertCorrectListBudgetSize(Session session, User user, TestArgumentContext argumentContext) {
        List<Budget> budgets = budgetRepository.selectBudgetRange(session, user, argumentContext.getFrom(), argumentContext.getTo());
        Assert.assertEquals(argumentContext.getRangeExpectedSize(), budgets.size(), 0);
    }

    /**
     * Метод создаеет разнородные контекстные данные для метода assertCorrectListBudgetSize
     */
    @Test
    public void testSelectBudgetRangeMethod() {
        User user = this.createUser(2L);
        List<TestArgumentContext> argumentContextList = List.of(
                new TestArgumentContext(YearMonth.of(2023, 1), YearMonth.of(2023, 2), 2),
                new TestArgumentContext(YearMonth.of(2023, 8), YearMonth.of(2023, 10), 2),
                new TestArgumentContext(YearMonth.of(2023, 4), YearMonth.of(2023, 5), 0),
                new TestArgumentContext(YearMonth.of(2023, 1), YearMonth.of(2023, 12), 8),
                new TestArgumentContext(YearMonth.of(2023, 12), YearMonth.of(2024, 1), 2)
        );
        transactionManager.produceTransaction(session -> {
            generateBudgetRange(session, user);
            for (TestArgumentContext argumentContext : argumentContextList) {
                assertCorrectListBudgetSize(session, user, argumentContext);
            }
        });
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
