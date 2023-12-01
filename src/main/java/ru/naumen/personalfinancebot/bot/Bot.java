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
    void startPooling() throws PollingException;

    /**
     * Отправка текстового сообщения определенному пользователю
     */
    void sendMessage(User user, String text);

    /**
     * Исключение, выбрасываемое в случае, если бот запустился с ошибкой
     */
    class PollingException extends Exception {
        /**
         * Стандартное сообщение об ошибке
         */
        private static final String DEFAULT_MESSAGE = "Произошла ошибка при запуске бота";

        /**
         * Конкретное сообщение об ошибке;
         */
        private final String exceptionMessage;

        public PollingException(String exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
        }

        public PollingException() {
            this(DEFAULT_MESSAGE);
        }

        public String getExceptionMessage() {
            return this.exceptionMessage;
        }
    }
}
