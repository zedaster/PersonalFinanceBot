package ru.naumen.personalfinancebot.repository;

import ru.naumen.personalfinancebot.models.User;

/**
 * Реализация хранилища пользователей в БД с помощью библиотеки Hibernate
 */
// TODO
public class HibernateUserRepository implements UserRepository {
    /**
     * Получает пользователя по chat id из telegram.
     */
    @Override
    public User getUserByTelegramChatId(Long chatId) {
        // TODO
        return null;
    }
}
