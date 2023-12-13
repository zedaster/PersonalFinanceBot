package ru.naumen.personalfinancebot.repositories.operation;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.TransactionManager;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repository.user.UserRepository;

import java.time.YearMonth;

public class HibernateOperationRepositoryTest {
    /**
     * Баланс пользователя
     */
    private final int BALANCE = 100_000;

    /**
     * Репозиторий для работы с пользователем
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий для работы с операциями
     */
    private final OperationRepository operationRepository;

    /**
     * Репозиторий для работы с катгоериями
     */
    private final CategoryRepository categoryRepository;

    /**
     * Менеджер транзакций
     */
    private final TransactionManager transactionManager;

    public HibernateOperationRepositoryTest() {
        SessionFactory sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.transactionManager = new TransactionManager(sessionFactory);
        this.userRepository = new HibernateUserRepository();
        this.operationRepository = new HibernateOperationRepository();
        this.categoryRepository = new HibernateCategoryRepository();
    }

    /**
     * Метод создает данные категорий и операций в базе данных
     * @param session Сессия
     */
    public void createCategoriesAndOperations(Session session) {
        User user = new User(1, BALANCE);
        this.userRepository.saveUser(session, user);
        Category salary, transfers, taxi, shops;
        try {
            salary = this.categoryRepository.createUserCategory(session, user, CategoryType.INCOME, "Зарплата");
            transfers = this.categoryRepository.createUserCategory(session, user, CategoryType.INCOME, "Перевод");
            taxi = this.categoryRepository.createUserCategory(session, user, CategoryType.EXPENSE, "Такси");
            shops = this.categoryRepository.createUserCategory(session, user, CategoryType.EXPENSE, "Продукты");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.operationRepository.addOperation(session, user, salary, 100_000);

        this.operationRepository.addOperation(session, user, transfers, 1_000);
        this.operationRepository.addOperation(session, user, transfers, 300);
        this.operationRepository.addOperation(session, user, transfers, 2_000);

        this.operationRepository.addOperation(session, user, taxi, 300);
        this.operationRepository.addOperation(session, user, taxi, 150);
        this.operationRepository.addOperation(session, user, taxi, 200);
        this.operationRepository.addOperation(session, user, taxi, 170);
        this.operationRepository.addOperation(session, user, taxi, 320);
        this.operationRepository.addOperation(session, user, taxi, 450);

        this.operationRepository.addOperation(session, user, shops, 700);
        this.operationRepository.addOperation(session, user, shops, 470);
        this.operationRepository.addOperation(session, user, shops, 560);
        this.operationRepository.addOperation(session, user, shops, 1500);
        this.operationRepository.addOperation(session, user, shops, 1200);
        this.operationRepository.addOperation(session, user, shops, 1032);
    }

    /**
     * Тест на корректный подсчет сумм операций
     */
    @Test
    public void getCurrentUserPaymentSummary() {
        transactionManager.produceTransaction(session -> {
            createCategoriesAndOperations(session);
            User user = this.userRepository.getUserByTelegramChatId(session,1L).get();
            double expectedIncome = 103_300, expectedExpense = 7052;
            YearMonth yearMonth = YearMonth.now();
            double income = this.operationRepository.getCurrentUserPaymentSummary(session, user, CategoryType.INCOME, yearMonth);

            double expense = this.operationRepository.getCurrentUserPaymentSummary(session, user, CategoryType.EXPENSE, yearMonth);
            Assert.assertEquals(expectedIncome, income, 1e-1);
            Assert.assertEquals(expectedExpense, expense, 1e-1);
        });
    }

    /**
     * Тестирует метод при условии что на данный момент нет никаких операций в базе данных
     */
    @Test
    public void getCurrentUserPaymentSummaryIfNoOperations() {
        transactionManager.produceTransaction(session -> {
            User user = new User(2L, BALANCE);
            this.userRepository.saveUser(session, user);

            YearMonth yearMonth = YearMonth.now();
            double income = this.operationRepository.getCurrentUserPaymentSummary(session, user, CategoryType.INCOME, yearMonth);
            double expense = this.operationRepository.getCurrentUserPaymentSummary(session, user, CategoryType.EXPENSE, yearMonth);

            Assert.assertEquals(0, income, 1e-10);
            Assert.assertEquals(0, expense, 1e-10);
        });
    }
}