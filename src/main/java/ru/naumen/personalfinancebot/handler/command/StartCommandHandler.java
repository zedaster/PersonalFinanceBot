package ru.naumen.personalfinancebot.handler.command;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
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
    public void handleCommand(HandleCommandEvent event) {
        event.getBot().sendMessage(event.getUser(), Message.WELCOME_MESSAGE);
    }
}
