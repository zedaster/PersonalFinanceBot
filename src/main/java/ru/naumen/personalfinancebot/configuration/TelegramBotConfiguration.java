package ru.naumen.personalfinancebot.configuration;

/**
 * Класс для настроек бота
 */
public class TelegramBotConfiguration {
    /**
     * Токен бота
     */
    private final String botToken;
    /**
     * Имя бота
     */
    private final String botName;


    /**
     * Конструктор с настройками из переменных окружения
     */
    public TelegramBotConfiguration() {
        this(
                System.getenv("BOT_TOKEN"),
                System.getenv("BOT_NAME")
        );
    }

    /**
     *
     * @param botToken токен бота
     * @param botName имя бота
     */
    public TelegramBotConfiguration(String botToken, String botName) {
        this.botToken = botToken;
        this.botName = botName;
    }

    /**
     * @return Токен бота
     */
    public String getBotToken() {
        return this.botToken;
    }

    /**
     * @return Имя бота
     */
    public String getBotName() {
        return this.botName;
    }
}