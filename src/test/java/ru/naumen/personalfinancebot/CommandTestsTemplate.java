package ru.naumen.personalfinancebot;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.FinanceBotHandler;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repository.user.UserRepository;
import ru.naumen.personalfinancebot.repository.TestHibernateCategoryRepository;

/**
 * Шаблон для создания последующих тестов для команд
 */
public class CommandTestsTemplate {
    // Static необходим для инициализации данных перед тестами и очистки после всех тестов
    private static final SessionFactory sessionFactory;
    private static final UserRepository userRepository;
    private static final TestHibernateCategoryRepository categoryRepository;
    private static final OperationRepository operationRepository;

    private final FinanceBotHandler botHandler;

    private User mockUser;
    private MockBot mockBot;

    // Инициализация статических полей перед использованием класса
    static {
        sessionFactory = new HibernateConfiguration().getSessionFactory();
        userRepository = new HibernateUserRepository(sessionFactory);
        categoryRepository = new TestHibernateCategoryRepository(sessionFactory);
        operationRepository = new HibernateOperationRepository(sessionFactory);
        // Добавить все стандартные значения здесь
    }

    public CommandTestsTemplate() {
        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository);
    }

    /**
     * Очистка стандартных значений и закрытие sessionFactory после выполнения всех тестов в этом классе
     */
    @AfterClass
    public static void finishTests() {
        categoryRepository.removeAll();
        sessionFactory.close();
    }

    /**
     * Создаем пользователя и бота перед каждым тестом
     */
    @Before
    public void beforeEachTest() {
        this.mockUser = new User(1L, 100);
        userRepository.saveUser(this.mockUser);
        this.mockBot = new MockBot();
    }

    /**
     * Удаляем пользователя из БД после каждого теста
     */
    @After
    public void afterEachTest() {
        userRepository.removeUserById(this.mockUser.getId());
    }

//    @Test
//    public void someTest() {
//        int someVar = 1;
//        Assert.assertEquals(1, someVar);
//    }
}
