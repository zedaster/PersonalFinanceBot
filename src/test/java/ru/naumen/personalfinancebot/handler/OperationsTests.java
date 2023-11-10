package ru.naumen.personalfinancebot.handler;

import org.junit.Assert;
import org.junit.Test;
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
    private static final double BALANCE = 100_000;

    private final UserRepository userRepository;
    private final OperationRepository operationRepository;
    private final CategoryRepository categoryRepository;
    private final BotHandler botHandler;

    public OperationsTests() {
        HibernateConfiguration hibernateUserRepository = new HibernateConfiguration();
        this.userRepository = new HibernateUserRepository(hibernateUserRepository.getSessionFactory());
        this.operationRepository = new HibernateOperationRepository(hibernateUserRepository.getSessionFactory());
        this.categoryRepository = new HibernateCategoryRepository(hibernateUserRepository.getSessionFactory());
        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository);
    }

    @Test
    public void addExpenseCommand() {
        String categoryName = "Такси";
        String categoryType = "Расход";
        String command = "add_expense";

        List<List<String>> argumentsList = getMockArgumentsList(categoryName);
        for (List<String> args: argumentsList){
            Category category = createCategory(categoryName, categoryType);
            assertCorrectOperation(category, command, args, CategoryType.EXPENSE);
        }
    }

    @Test
    public void addIncomeCommand() {
        String categoryName = "Зарплата";
        String categoryType = "Доход";
        String command = "add_income";

        List<List<String>> argumentsList = getMockArgumentsList(categoryName);
        for (List<String> args: argumentsList){
            Category category = createCategory(categoryName, categoryType);
            assertCorrectOperation(category, command, args, CategoryType.INCOME);
        }
    }

    private List<List<String>> getMockArgumentsList(String categoryName) {
        return new ArrayList<>(){
            {
                add(List.of("25000", categoryName));
                add(List.of("100",   categoryName));
                add(List.of("32525", categoryName));
                add(List.of("23423", categoryName));
                add(List.of("12345", categoryName));
            }
        };
    }

    private void assertCorrectOperation(Category category, String command, List<String> args, CategoryType type) {
        User user = new User(1, BALANCE);
        this.userRepository.saveUser(user);
        double payment = getBeautifyPayment(args.get(0), type);
        MockBot bot = new MockBot();

        HandleCommandEvent commandEvent = new HandleCommandEvent(bot, user, command, args);
        this.botHandler.handleCommand(commandEvent);
        MockMessage message = bot.poolMessageQueue();
        Assert.assertEquals(user.getId(), message.sender().getId());
        assert BALANCE + payment == user.getBalance();
        this.userRepository.removeUserById(user.getId());
        this.categoryRepository.deleteCategoryById(category.getId());
    }

    private Category createCategory(String categoryName, String type) {
        return this.categoryRepository.createStandartCategory(categoryName, type);
    }

    private double getBeautifyPayment(String payment, CategoryType type) {
        double result = Double.parseDouble(payment);
        if (type == CategoryType.EXPENSE) {
            return - Math.abs(result);
        }
        return Math.abs(result);
    }
}
