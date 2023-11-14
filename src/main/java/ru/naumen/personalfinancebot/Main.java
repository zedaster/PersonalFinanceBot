package ru.naumen.personalfinancebot;

import ru.naumen.personalfinancebot.bot.Bot;
import ru.naumen.personalfinancebot.bot.TelegramBot;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.configuration.TelegramBotConfiguration;
import ru.naumen.personalfinancebot.handler.BotHandler;
import ru.naumen.personalfinancebot.handler.FinanceBotHandler;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;

/**
 * Программа, запускающая Телеграм-бота
 */
public class Main {
    public static void main(String[] args) {
        HibernateConfiguration hibernateConfiguration = new HibernateConfiguration();
        UserRepository userRepository = new HibernateUserRepository(hibernateConfiguration.getSessionFactory());
        OperationRepository operationRepository = new HibernateOperationRepository(hibernateConfiguration.getSessionFactory());
        CategoryRepository categoryRepository = new HibernateCategoryRepository(hibernateConfiguration.getSessionFactory());
        BotHandler handler = new FinanceBotHandler(
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
        bot.startPooling();
    }
}
