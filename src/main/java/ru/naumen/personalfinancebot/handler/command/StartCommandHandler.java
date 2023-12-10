package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;

/**
 * Обработчик стартовой команды
 *
 * @author Sergey Kazantsev
 */
public class StartCommandHandler implements CommandHandler {
    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(CommandData commandData, Session session) {
        commandData.getBot().sendMessage(commandData.getUser(), Message.WELCOME_MESSAGE);
    }
}
