package ru.naumen.personalfinancebot.repository.empty;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.user.UserRepository;

import java.util.Optional;

/**
 * Хранилище с пользователями, которое при любой операции бросит ${@link RuntimeException}
 */
public class EmptyUserRepository implements UserRepository {
    @Override
    public Optional<User> getUserByTelegramChatId(Session session, Long chatId) {
        throw new RuntimeException("User repository shouldn't be touched");
    }

    @Override
    public void saveUser(Session session, User user) {
        throw new RuntimeException("User repository shouldn't be touched");

    }

    @Override
    public void removeUserById(Session session, long id) {
        throw new RuntimeException("User repository shouldn't be touched");
    }
}
