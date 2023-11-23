package ru.naumen.personalfinancebot.repository.empty;

import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;

import java.util.Optional;

/**
 * Хранилище с пользователями, которое при любой операции бросит ${@link RuntimeException}
 */
public class EmptyUserRepository implements UserRepository {
    @Override
    public Optional<User> getUserByTelegramChatId(Long chatId) {
        throw new RuntimeException("User repository shouldn't be touched");
    }

    @Override
    public void saveUser(User user) {
        throw new RuntimeException("User repository shouldn't be touched");

    }

    @Override
    public void removeUserById(long id) {
        throw new RuntimeException("User repository shouldn't be touched");
    }
}
