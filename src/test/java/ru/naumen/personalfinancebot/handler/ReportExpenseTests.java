package ru.naumen.personalfinancebot.handler;

import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;
import ru.naumen.personalfinancebot.services.ReportService;

import java.util.*;

/**
 * Класс для тестирования команда "/report_expense"
 * Проверка отчетов за месяц.
 */
public class ReportExpenseTests {
    private static final int MIN_PAYMENT = 10_000;
    private static final int MAX_PAYMENT = 100_000;

    /**
     * Репозиторий для работы с пользователем
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий для работы с операциями
     * ! Нужен для FinanceBotHandler;
     */
    private final OperationRepository operationRepository;

    /**
     * Репозиторий для работы с категориями
     */
    private final CategoryRepository categoryRepository;

    /**
     * Класс для подготовки отчётов по расходам
     */
    private final ReportService reportService;

    /**
     * Обработчик операций в боте
     */
    private final BotHandler botHandler;

    public ReportExpenseTests() {
        HibernateConfiguration hibernateUserRepository = new HibernateConfiguration();
        this.userRepository = new HibernateUserRepository(hibernateUserRepository.getSessionFactory());
        this.operationRepository = new HibernateOperationRepository(hibernateUserRepository.getSessionFactory());
        this.categoryRepository = new HibernateCategoryRepository(hibernateUserRepository.getSessionFactory());
        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository);
        this.reportService = new ReportService(this.operationRepository);
    }

    /**
     * Возвращает список операций
     *
     * @param user           Пользователь
     * @param categoriesName список с названиями категорий
     * @return список категорий
     */
    private List<Category> fillExpenseOperation(User user, List<String> categoriesName) {
        List<Category> categories = new ArrayList<>();
        for (String categoryName : categoriesName) {
            Category category = categoryRepository.createCategory(categoryName, "Расход", user);
            categories.add(category);
        }
        return categories;
    }

    private List<Operation> getOperationsList(User user, List<Category> categories) {
        List<Operation> operations = new ArrayList<>();
        for (Category category : categories) {
            for (int i = 0; i < 3; i++) {
                Operation operation = this.operationRepository.addOperation(user, category, getRandomPayment());
                operations.add(operation);
            }
        }
        return operations;
    }

    @Test
    public void checkReportMap() {
        User user = new User(1, 100_000);
        this.userRepository.saveUser(user);
        List<String> categoriesNames = List.of("Продукты", "Такси", "Аптеки", "Развлечения", "Автосервис");
        List<Category> categories = fillExpenseOperation(user, categoriesNames);
        List<Operation> operations = getOperationsList(user, categories);
        Map<String, Double> map = new HashMap<>();
        for (Operation operation : operations) {
            map.put(
                    operation.getCategory().getCategoryName(),
                    map.getOrDefault(
                            operation.getCategory().getCategoryName(),
                            0.0
                    ) + operation.getPayment());
        }
        List<String> args = List.of("11.2023".split("\\."));
        Map<String, Double> serviceMap = this.reportService.getExpenseReport(user, args);

        for (Map.Entry<String, Double> entry : map.entrySet()) {
            Assert.assertEquals(entry.getValue(), serviceMap.get(entry.getKey()), 1e-2);
        }
    }

    private Double getRandomPayment() {
        Random random = new Random();
        return MIN_PAYMENT + (MAX_PAYMENT - MIN_PAYMENT) * random.nextDouble();
    }
}
