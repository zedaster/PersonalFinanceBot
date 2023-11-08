package ru.naumen.personalfinancebot.bot;

import ru.naumen.personalfinancebot.models.User;

/**
 * Описание методов для бота
 */
public interface Bot {
    /**
     * Запуск бота
     */
    void startPooling();

    /**
     * Отправка текстового сообщения определенному пользователю
     */
    void sendMessage(User user, String text);
}
