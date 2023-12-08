package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;

/**
 * Обработчик команды для бота
 */
public interface CommandHandler {
    /**
     * Метод, вызываемый при получении команды
     */
    void handleCommand(CommandData event, Session session);
}
