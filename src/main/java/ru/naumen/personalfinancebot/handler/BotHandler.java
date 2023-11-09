package ru.naumen.personalfinancebot.handler;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;

/**
 * Обработчик операций в боте
 */
public interface BotHandler {
    /**
     * Вызывается при получении какой-либо команды от пользователя
     */
    void handleCommand(HandleCommandEvent event);
}
