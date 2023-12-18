package ru.naumen.personalfinancebot.bot;

import ru.naumen.personalfinancebot.model.User;

/**
 * Описание методов для бота
 */
public interface Bot {
    /**
     * Запуск бота
     */
    void startPooling() throws PoolingException;

    /**
     * Отправка текстового сообщения определенному пользователю
     */
    void sendMessage(User user, String text);
}
