package ru.naumen.personalfinancebot;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.FinanceBotHandler;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.TestHibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.TransactionManager;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repository.user.UserRepository;

/**
 * Шаблон для создания последующих тестов для команд
 */
public class CommandTestsTemplate {
    // Static необходим для инициализации данных перед тестами и очистки после всех тестов
    private static final SessionFactory sessionFactory;
    private static final UserRepository userRepository;
    private static final TestHibernateCategoryRepository categoryRepository;
    private static final OperationRepository operationRepository;
    private static final TransactionManager transactionManager;

    // Инициализация статических полей перед использованием класса
    static {
        sessionFactory = new HibernateConfiguration().getSessionFactory();
        transactionManager = new TransactionManager(sessionFactory);
        userRepository = new HibernateUserRepository();
        categoryRepository = new TestHibernateCategoryRepository();
        operationRepository = new HibernateOperationRepository();
        // Добавить все стандартные значения здесь
    }

    private final FinanceBotHandler botHandler;
    private User mockUser;
    private MockBot mockBot;

    public CommandTestsTemplate() {
        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository, sessionFactory);
    }

    /**
     * Очистка стандартных значений и закрытие sessionFactory после выполнения всех тестов в этом классе
     */
    @AfterClass
    public static void finishTests() {
        transactionManager.produceTransaction(categoryRepository::removeAll);
    }

    /**
     * Создаем пользователя и бота перед каждым тестом
     */
    @Before
    public void beforeEachTest() {
        transactionManager.produceTransaction(session -> {
            this.mockUser = new User(1L, 100);
            userRepository.saveUser(session, this.mockUser);
            this.mockBot = new MockBot();
        });
    }

    /**
     * Удаляем пользователя из БД после каждого теста
     */
    @After
    public void afterEachTest() {
        transactionManager.produceTransaction(session -> {
            userRepository.removeUserById(session, this.mockUser.getId());
        });
    }
}
