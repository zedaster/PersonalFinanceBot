package ru.naumen.personalfinancebot.bot;

import ru.naumen.personalfinancebot.configuration.TelegramBotConfiguration;
import ru.naumen.personalfinancebot.handler.BotHandler;
import ru.naumen.personalfinancebot.models.User;

/**
 * Телеграм бот
 */
public class TelegramBot implements Bot {

    public TelegramBot(TelegramBotConfiguration configuration, BotHandler handler) {
        // TODO
    }

    /**
     * Запуск бота
     */
    @Override
    public void startPooling() {
        // TODO

    }

    /**
     * Отправка текстового сообщения определенному пользователю
     */
    @Override
    public void sendMessage(User user, String text) {
        // TODO

    }
}
