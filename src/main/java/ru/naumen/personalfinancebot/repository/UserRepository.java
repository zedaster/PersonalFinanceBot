package ru.naumen.personalfinancebot.repository;

import ru.naumen.personalfinancebot.models.User;

import java.util.Optional;

/**
 * Хранилище для пользователей
 */
public interface UserRepository {
    /**
     * Получает пользователя по chat id из telegram.
     */
    Optional<User> getUserByTelegramChatId(Long chatId);

    /**
     * Сохраняет существующего или нового юзера в БД
     */
    void saveUser(User user);

    /**
     * Удаляет существующего юзера под его id в БД (не telegram id)
     */
    void removeUserById(long id);
}
