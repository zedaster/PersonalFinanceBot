package ru.naumen.personalfinancebot.bot;

/**
 * Исключение, выбрасываемое в случае, если бот запустился с ошибкой
 */
public class PoolingException extends Exception {
    public PoolingException(String message, Throwable cause) {
        super(message, cause);
    }
}
