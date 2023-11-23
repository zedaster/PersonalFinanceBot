package ru.naumen.personalfinancebot.repositories.operation;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;

import java.time.YearMonth;

public class HibernateOperationRepositoryTest {
    private static final int BALANCE = 100_000;

    private final SessionFactory sessionFactory;
    private final UserRepository userRepository;
    private final OperationRepository operationRepository;
    private final CategoryRepository categoryRepository;

    public HibernateOperationRepositoryTest() {
        this.sessionFactory = new HibernateConfiguration().getSessionFactory();
        this.userRepository = new HibernateUserRepository(sessionFactory);
        this.operationRepository = new HibernateOperationRepository(sessionFactory);
        this.categoryRepository = new HibernateCategoryRepository(sessionFactory);
    }

    public void createCategoriesAndOperations() throws CategoryRepository.CreatingExistingUserCategoryException, CategoryRepository.CreatingExistingStandardCategoryException {
        User user = new User(1, BALANCE);
        this.userRepository.saveUser(user);
        Category salary = this.categoryRepository.createUserCategory(user, CategoryType.INCOME, "Зарплата");
        Category transfers = this.categoryRepository.createUserCategory(user, CategoryType.INCOME, "Перевод");
        Category taxi = this.categoryRepository.createUserCategory(user, CategoryType.EXPENSE, "Такси");
        Category shops = this.categoryRepository.createUserCategory(user, CategoryType.EXPENSE, "Продукты");
        this.operationRepository.addOperation(user, salary, 100_000);

        this.operationRepository.addOperation(user, transfers, 1_000);
        this.operationRepository.addOperation(user, transfers, 300);
        this.operationRepository.addOperation(user, transfers, 2_000);

        this.operationRepository.addOperation(user, taxi, 300);
        this.operationRepository.addOperation(user, taxi, 150);
        this.operationRepository.addOperation(user, taxi, 200);
        this.operationRepository.addOperation(user, taxi, 170);
        this.operationRepository.addOperation(user, taxi, 320);
        this.operationRepository.addOperation(user, taxi, 450);

        this.operationRepository.addOperation(user, shops, 700);
        this.operationRepository.addOperation(user, shops, 470);
        this.operationRepository.addOperation(user, shops, 560);
        this.operationRepository.addOperation(user, shops, 1500);
        this.operationRepository.addOperation(user, shops, 1200);
        this.operationRepository.addOperation(user, shops, 1032);
    }

    @Test
    public void getCurrentUserPaymentSummary() {
        try {
            createCategoriesAndOperations();
        } catch (Exception ignored) {}
        User user = this.userRepository.getUserByTelegramChatId(1L).get();
        double expectedIncome = 103_300, expectedExpense = 7052;
        YearMonth yearMonth = YearMonth.of(2023, 11);
        double income = this.operationRepository.getCurrentUserPaymentSummary(user, CategoryType.INCOME, yearMonth);

        double expense = this.operationRepository.getCurrentUserPaymentSummary(user, CategoryType.EXPENSE, yearMonth);
        Assert.assertEquals(expectedIncome, income, 1e-1);
        Assert.assertEquals(expectedExpense, expense, 1e-1);
    }
}