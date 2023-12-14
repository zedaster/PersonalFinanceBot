package ru.naumen.personalfinancebot.repository.user;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.User;

import java.util.Optional;

/**
 * Хранилище для пользователей
 */
public interface UserRepository {
    /**
     * Получает пользователя по chat id из telegram.
     */
    Optional<User> getUserByTelegramChatId(Session session, Long chatId);

    /**
     * Сохраняет существующего или нового юзера в БД
     */
    void saveUser(Session session, User user);

    /**
     * Удаляет существующего юзера под его id в БД (не telegram id)
     */
    void removeUserById(Session session, long id);
}
