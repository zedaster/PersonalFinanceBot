package ru.naumen.personalfinancebot.handler;

import com.sun.istack.Nullable;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.repository.UserRepository;

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
    private final Map<String, Consumer<HandleCommandEvent>> handlers;

    private final UserRepository userRepository;

    public FinanceBotHandler(UserRepository userRepository) {
        this.handlers = new HashMap<>();
        this.handlers.put("set_balance", this::handleSetBalance);
        this.handlers.put("add_expense", this::handleAddExpense);
        this.handlers.put("category_add", this::handleCategoryAdd);
        this.handlers.put("category_remove", this::handleCategoryRemove);
        this.handlers.put("category_list", this::handleCategoryList);
        // TODO Добавить больше обработчиков

        this.userRepository = userRepository;
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
        double amount;
        try {
            if (event.getArgs().size() != 1) throw new IllegalArgumentException();
            amount = parseBalanceAmount(event.getArgs().get(0));
        } catch (IllegalArgumentException e) {
            event.getBot().sendMessage(event.getUser(),
                    "Команда введена неверно! Введите /set_balance <новый баланс>");
            return;
        }

        event.getUser().setBalance(amount);
        userRepository.saveUser(event.getUser());
        event.getBot().sendMessage(event.getUser(), "Ваш баланс изменен. Теперь он составляет " +
                beautifyDouble(amount));
    }


    /**
     * Команда для добавления трат
     */
    private void handleAddExpense(HandleCommandEvent event) {
        // TODO
    }

    // TODO Писать обработчики дальше и тесты для них

    /**
     * Команда для добавления пользовательской категории
     */
    private void handleCategoryAdd(HandleCommandEvent event) {
        // TODO
//        Category category = new Category(event.getUser(), )
    }

    /**
     * Команда для удаления пользовательской категории
     */
    private void handleCategoryRemove(HandleCommandEvent event) {
        // TODO

    }

    /**
     * Команда для вывода категории пользователем
     */
    private void handleCategoryList(HandleCommandEvent event) {
        // TODO
    }

    /**
     * Парсит баланс, введенный пользователем
     * Вернет null если баланс не является числом с плавающей точкой или меньше нуля
     */
    @Nullable
    private Double parseBalanceAmount(String string) throws NumberFormatException {
        double amount = Double.parseDouble(string.replace(",", "."));
        if (amount < 0) throw new NumberFormatException();
        return amount;
    }

    /**
     * Форматирует double в красивую строку
     * Если число целое, то вернет его без дробной части.
     * Т.е. 1000.0 будет выведено как 1000
     * А 1000.99 будет выведено как 1000.99
     */
    private String beautifyDouble(double d) {
        if ((int) d == d) return String.valueOf((int) d);
        return String.valueOf(d);
    }

}
