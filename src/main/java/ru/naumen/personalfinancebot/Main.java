package ru.naumen.personalfinancebot;

import ru.naumen.personalfinancebot.bot.Bot;
import ru.naumen.personalfinancebot.bot.TelegramBot;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.configuration.TelegramBotConfiguration;
import ru.naumen.personalfinancebot.handler.FinanceBotHandler;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;

/**
 * Программа, запускающая Телеграм-бота
 */
public class Main {
    public static void main(String[] args) {
        HibernateConfiguration hibernateConfiguration = new HibernateConfiguration(
                System.getenv("DB_URL"),
                System.getenv("DB_USERNAME"),
                System.getenv("DB_PASSWORD"));
        UserRepository userRepository = new HibernateUserRepository(hibernateConfiguration.getSessionFactory());
        OperationRepository operationRepository = new HibernateOperationRepository(hibernateConfiguration.getSessionFactory());
        CategoryRepository categoryRepository = new HibernateCategoryRepository(hibernateConfiguration.getSessionFactory());
        FinanceBotHandler handler = new FinanceBotHandler(
                userRepository,
                operationRepository,
                categoryRepository
        );
        TelegramBotConfiguration configuration = new TelegramBotConfiguration();
        Bot bot = new TelegramBot(
                configuration,
                handler,
                userRepository
        );
        try {
            bot.startPooling();
        } catch (Bot.PoolingException exception) {
            exception.printStackTrace();
        }
    }
}
