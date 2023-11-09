package ru.naumen.personalfinancebot;

import ru.naumen.personalfinancebot.bot.Bot;
import ru.naumen.personalfinancebot.bot.TelegramBot;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.configuration.TelegramBotConfiguration;
import ru.naumen.personalfinancebot.handler.BotHandler;
import ru.naumen.personalfinancebot.handler.FinanceBotHandler;
import ru.naumen.personalfinancebot.repository.HibernateUserRepository;
import ru.naumen.personalfinancebot.repository.UserRepository;

/**
 * Программа, запускающая Телеграм-бота
 */
public class Main {
    public static void main(String[] args) {
        HibernateConfiguration hibernateConfiguration = new HibernateConfiguration();
        UserRepository userRepository = new HibernateUserRepository(hibernateConfiguration.getSessionFactory());
        BotHandler handler = new FinanceBotHandler(userRepository);
        TelegramBotConfiguration configuration = new TelegramBotConfiguration();
        Bot bot = new TelegramBot(configuration, handler, userRepository);
        bot.startPooling();
    }
}
