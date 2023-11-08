package ru.naumen.personalfinancebot.handler;

import ru.naumen.personalfinancebot.bot.Bot;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.User;

import java.util.List;

/**
 * Обработчик операций в боте
 */
public interface BotHandler {
    /**
     * Вызывается при получении какой-либо команды от пользователя
     */
    void handleCommand(HandleCommandEvent event);
}
