package ru.naumen.personalfinancebot;

import ru.naumen.personalfinancebot.bot.Bot;
import ru.naumen.personalfinancebot.bot.TelegramBot;
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
        BotHandler handler = new FinanceBotHandler();
        TelegramBotConfiguration configuration = new TelegramBotConfiguration();
        UserRepository userRepository = new HibernateUserRepository();
        Bot bot = new TelegramBot(configuration, handler, userRepository);
        bot.startPooling();
    }
}
