package ru.naumen.personalfinancebot.handler;

import org.junit.Assert;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class OperationsTests {
    /**
     * Статическое поле для начального баланса
     */
    private static final double BALANCE = 100_000;

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
     * Обработчик операций в боте
     */
    private final BotHandler botHandler;

    // TODO: Леше как-то протестировать двух юзеров

    public OperationsTests() {
        HibernateConfiguration hibernateUserRepository = new HibernateConfiguration();
        this.userRepository = new HibernateUserRepository(hibernateUserRepository.getSessionFactory());
        this.operationRepository = new HibernateOperationRepository(hibernateUserRepository.getSessionFactory());
        this.categoryRepository = new HibernateCategoryRepository(hibernateUserRepository.getSessionFactory());
        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository);
    }

    /**
     * Тест для команды "/add_expense@
     */
    // TODO: Исправить Леше
//    @Test
//    public void addExpenseCommand() {
//        String categoryName = "Такси";
//        String categoryType = "Расход";
//        String command = "add_expense";
//
//        List<List<String>> argumentsList = getMockArgumentsList(categoryName);
//        for (List<String> args : argumentsList) {
//            Category category = createCategory(categoryName, categoryType);
//            assertCorrectOperation(category, command, args, CategoryType.EXPENSE);
//        }
//    }

    /**
     * Тест для команда "/add_income"
     */
    // TODO: Исправить Леше
//    @Test
//    public void addIncomeCommand() {
//        String categoryName = "Зарплата";
//        String categoryType = "Доход";
//        String command = "add_income";
//
//        List<List<String>> argumentsList = getMockArgumentsList(categoryName);
//        for (List<String> args : argumentsList) {
//            Category category = createCategory(categoryName, categoryType);
//            assertCorrectOperation(category, command, args, CategoryType.INCOME);
//        }
//    }

    /**
     * Возвращает моковый лист аргументов для теста
     *
     * @param categoryName Имя категории
     * @return Лист аргументов
     */
    private List<List<String>> getMockArgumentsList(String categoryName) {
        return new ArrayList<>() {
            {
                add(List.of("0", categoryName));
                add(List.of("1", categoryName));
                add(List.of(String.valueOf(Double.MAX_VALUE), categoryName));
                add(List.of("Число", categoryName));
                add(List.of("-10", categoryName));
            }
        };
    }

    /**
     * Проверяет корректность баланса и пользователя, которому должно быть отправлено сообщение.
     *
     * @param category Категори
     * @param command  Команда
     * @param args     Лист аргументов
     * @param type     Тип категории
     */
    private void assertCorrectOperation(Category category, String command, List<String> args, CategoryType type)
            throws CategoryRepository.RemovingStandardCategoryException {
        User user = new User(1, BALANCE);
        this.userRepository.saveUser(user);
        double payment = getBeautifyPayment(args.get(0), type);
        MockBot bot = new MockBot();

        HandleCommandEvent commandEvent = new HandleCommandEvent(bot, user, command, args);
        this.botHandler.handleCommand(commandEvent);
        MockMessage message = bot.poolMessageQueue();
        Assert.assertEquals(user.getId(), message.receiver().getId());
        Assert.assertEquals(BALANCE + payment, user.getBalance(), 1e-3);
        this.userRepository.removeUserById(user.getId());
        this.categoryRepository.removeCategoryById(category.getId());
    }

    /**
     * Создает и возвращает категорию
     *
     * @param categoryName Имя категории
     * @param type Тип категории
     * @return Категория
     */
//    private Category createCategory(String categoryName, String type) {
//        // TODO: Тесты Лёше исправить
////        return this.categoryRepository.createStandartCategory(type, categoryName);
//    }

    /**
     * Парсит переданное число к типу double, в случае исключения возвращает 0
     *
     * @param payment Число в виде строки
     * @param type    Тип категории
     * @return Число
     */
    private double getBeautifyPayment(String payment, CategoryType type) {
        // TODO: Лёшё исправить. Баланс просили указывать явно
        double result;
        try {
            result = Double.parseDouble(payment);
        } catch (NumberFormatException e) {
            return 0.0;
        }
        if (type == CategoryType.EXPENSE) {
            return -Math.abs(result);
        }
        return Math.abs(result);
    }
}
