package ru.naumen.personalfinancebot.bot;

import ru.naumen.personalfinancebot.models.User;

/**
 * Представляет из себе класс сообщения для мокового бота
 * Содержит в себе констуктор, геттеры, а также методы equals, hashCode, toString
 */
public record MockMessage(User sender, String text) {
}
