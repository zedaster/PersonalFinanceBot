package ru.naumen.personalfinancebot.handler.commandData;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.bot.Bot;
import ru.naumen.personalfinancebot.model.User;

import java.util.List;

/**
 * Событие, вызываемое при выполнении пользователем какой-либо команды
 */
public class CommandData {
    /**
     * Бот, который обрабатывает команду
     */
    private final Bot bot;

    /**
     * Пользователь, который отправил команду
     */
    private final User user;

    /**
     * Название команды
     */
    private final String commandName;

    /**
     * Список аргументов к команде
     */
    private final List<String> args;

    public CommandData(Bot bot, User user, String commandName, List<String> args) {
        this.bot = bot;
        this.user = user;
        this.commandName = commandName;
        this.args = args;
    }

    /**
     * Получает бота, который обрабатывает команду
     */
    public Bot getBot() {
        return bot;
    }

    /**
     * Получает пользователя, который отправил команду
     */
    public User getUser() {
        return user;
    }

    /**
     * Получает название команды
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Получает список аргументов к команде
     */
    public List<String> getArgs() {
        return args;
    }
}
