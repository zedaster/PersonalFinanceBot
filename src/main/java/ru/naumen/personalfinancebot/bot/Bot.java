package ru.naumen.personalfinancebot.bot;

import com.sun.istack.Nullable;
import org.apache.commons.lang3.ObjectUtils;
import ru.naumen.personalfinancebot.models.User;

import java.util.Objects;

/**
 * Описание методов для бота
 */
public interface Bot {
    /**
     * Запуск бота
     */
    void startPooling() throws PoolingException;

    /**
     * Отправка текстового сообщения определенному пользователю
     */
    void sendMessage(User user, String text);

    /**
     * Исключение, выбрасываемое в случае, если бот запустился с ошибкой
     */
    class PoolingException extends Exception {
        /**
         * Стандартное сообщение об ошибке
         */
        private static final String DEFAULT_MESSAGE = "Произошла ошибка при запуске бота";

        /**
         * Конкретное сообщение об ошибке;
         */
        private final String exceptionMessage;

        /**
         * Исключение, которое было выброшено во время запуска бота
         */
        private final Throwable cause;

        public PoolingException(Throwable cause) {
            this(cause, DEFAULT_MESSAGE);
        }

        public PoolingException(Throwable cause, String exceptionMessage) {
            this.cause = cause;
            this.exceptionMessage = exceptionMessage;
            System.out.println(this.exceptionMessage);
            System.out.print("Сообщение исключения: " + this.cause.getMessage());
        }
    }
}
