package ru.naumen.personalfinancebot.handler;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Обработчик операций для бота "Персональный финансовый трекер"
 */
public class FinanceBotHandler implements BotHandler {
    /**
     * Коллекция, которая хранит обработчики для каждой команды
     */
    private Map<String, Consumer<HandleCommandEvent>> handlers;

    public FinanceBotHandler() {
        this.handlers = new HashMap<>();
        this.handlers.put("set_balance", this::handleSetBalance);
        this.handlers.put("add_expense", this::handleAddExpense);
        // TODO Добавить больше обработчиков
    }

    /**
     * Вызывается при получении какой-либо команды от пользователя
     */
    @Override
    public void handleCommand(HandleCommandEvent event) {
        Consumer<HandleCommandEvent> handler = this.handlers.get(event.getCommandName().toLowerCase());
        if (handler != null) {
            handler.accept(event);
        }
        // TODO Действия при handler == null
    }

    /**
     * Команда для установки баланса
     */
    private void handleSetBalance(HandleCommandEvent event) {
        // TODO
    }

    /**
     * Команда для добавления трат
     */
    private void handleAddExpense(HandleCommandEvent event) {
        // TODO
    }

    // TODO Писать обработчики дальше и тесты для них
}
