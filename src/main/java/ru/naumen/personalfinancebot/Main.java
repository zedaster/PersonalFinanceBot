package ru.naumen.personalfinancebot;

import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.bot.Bot;
import ru.naumen.personalfinancebot.bot.PoolingException;
import ru.naumen.personalfinancebot.bot.TelegramBot;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.configuration.StandardCategoryConfiguration;
import ru.naumen.personalfinancebot.configuration.TelegramBotConfiguration;
import ru.naumen.personalfinancebot.handler.FinanceBotHandler;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.repository.TransactionManager;
import ru.naumen.personalfinancebot.repository.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repository.budget.HibernateBudgetRepository;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;
import ru.naumen.personalfinancebot.repository.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.repository.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repository.user.UserRepository;

import java.util.List;

/**
 * Программа, запускающая Телеграм-бота
 */
public class Main {
    public static void main(String[] args) {
        HibernateConfiguration hibernateConfiguration = new HibernateConfiguration(
                System.getenv("DB_URL"),
                System.getenv("DB_USERNAME"),
                System.getenv("DB_PASSWORD"));
        SessionFactory sessionFactory = hibernateConfiguration.getSessionFactory();
        TransactionManager transactionManager = new TransactionManager(sessionFactory);

        List<Category> standardCategories = new StandardCategoryConfiguration().getStandardCategories();

        UserRepository userRepository = new HibernateUserRepository();
        OperationRepository operationRepository = new HibernateOperationRepository();
        CategoryRepository categoryRepository = new HibernateCategoryRepository(transactionManager, standardCategories);
        BudgetRepository budgetRepository = new HibernateBudgetRepository();

        FinanceBotHandler handler = new FinanceBotHandler(
                userRepository,
                operationRepository,
                categoryRepository,
                budgetRepository,
                sessionFactory
        );

        TelegramBotConfiguration configuration = new TelegramBotConfiguration();
        Bot bot = new TelegramBot(
                configuration,
                handler,
                userRepository,
                transactionManager
        );

        try {
            bot.startPooling();
        } catch (PoolingException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
    }
}
