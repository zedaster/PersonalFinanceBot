package ru.naumen.personalfinancebot.bot;

import ru.naumen.personalfinancebot.model.User;

/**
 * Представляет из себя класс сообщения для мокового бота.
 * Содержит в себе констуктор, геттеры, а также методы equals, hashCode, toString
 */
public record MockMessage(User receiver, String text) {
}
