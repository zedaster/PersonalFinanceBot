package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;

/**
 * Обработчик команды для бота
 */
public interface CommandHandler {
    /**
     * Метод, вызываемый при получении команды
     */
    void handleCommand(HandleCommandEvent event);
}
