package ru.naumen.personalfinancebot.repository;

import ru.naumen.personalfinancebot.models.User;

/**
 * Хранилище для пользователей
 */
public interface UserRepository {
    /**
     * Получает пользователя по chat id из telegram.
     */
    User getUserByTelegramChatId(Long chatId);
}
