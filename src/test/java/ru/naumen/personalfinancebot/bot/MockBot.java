package ru.naumen.personalfinancebot.bot;

import ru.naumen.personalfinancebot.models.User;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Моковый бот, который позволяет получать сообщения, отправленные через него.
 * История сообщения храниться в формате очередь
 */
public class MockBot implements Bot {
    /**
     * Очередь, в которую помещаються отправленные сообщения
     */
    private final Queue<MockMessage> messageQueue;

    public MockBot() {
        this.messageQueue = new LinkedList<>();
    }

    /**
     * Этот метод мокового бота ничего не делает
     */
    @Override
    public void startPooling() {
        // empty
    }

    /**
     * Отправка сообщения
     */
    @Override
    public void sendMessage(User user, String text) {
        MockMessage message = new MockMessage(user, text);
        messageQueue.add(message);
    }

    /**
     * Получения размера очереди
     */
    public int getMessageQueueSize() {
        return this.messageQueue.size();
    }

    /**
     * Получение и удаление первого сообщения в очереди
     */
    public MockMessage poolMessageQueue() {
        return this.messageQueue.poll();
    }
}
