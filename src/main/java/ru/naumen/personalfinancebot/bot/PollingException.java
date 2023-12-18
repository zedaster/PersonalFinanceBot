package ru.naumen.personalfinancebot.bot;

/**
 * Исключение, выбрасываемое в случае, если бот запустился с ошибкой
 */
public class PollingException extends Exception {
    public PollingException(String message, Throwable cause) {
        super(message, cause);
    }
}
