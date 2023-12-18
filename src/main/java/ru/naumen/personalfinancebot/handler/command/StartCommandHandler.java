package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;

/**
 * Обработчик стартовой команды
 *
 * @author Sergey Kazantsev
 */
public class StartCommandHandler implements CommandHandler {
    /**
     * Сообщение - приветствие для пользователя
     */
    private static final String WELCOME_MESSAGE = "Добро пожаловать в бота для управления финансами!";

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        commandData.getBot().sendMessage(commandData.getUser(), WELCOME_MESSAGE);
    }
}
