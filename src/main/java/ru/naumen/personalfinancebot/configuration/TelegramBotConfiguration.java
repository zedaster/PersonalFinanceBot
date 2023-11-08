package ru.naumen.personalfinancebot.configuration;

/**
 * Конфигурация для телеграм бота
 */
public class TelegramBotConfiguration {
    /**
     * Токен бота
     */
    private final String botToken;

    /**
     * Юзернейм бота
     */
    private final String botUsername;

    public TelegramBotConfiguration() {
        // TODO
    }

    /**
     * Выдает токен бота
     */
    public String getBotToken() {
        return botToken;
    }

    /**
     * Выдает юзернейм бота
     */
    public String getBotUsername() {
        return botUsername;
    }
}
