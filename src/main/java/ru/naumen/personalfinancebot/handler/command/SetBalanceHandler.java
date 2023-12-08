package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.repository.user.UserRepository;
import ru.naumen.personalfinancebot.service.ArgumentParseService;

/**
 * Обработчик команды для установки баланса
 *
 * @author Sergey Kazantsev
 */
public class SetBalanceHandler implements CommandHandler {
    /**
     * Сервис, который парсит аргументы
     */
    private final ArgumentParseService argumentParser;

    /**
     * Хранилище пользователей
     */
    private final UserRepository userRepository;

    public SetBalanceHandler(ArgumentParseService argumentParser, UserRepository userRepository) {
        this.argumentParser = argumentParser;
        this.userRepository = userRepository;
    }

    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(CommandData commandData, Session session) {
        double amount;
        try {
            amount = argumentParser.parseBalance(commandData.getArgs());
        } catch (IllegalArgumentException e) {
            commandData.getBot().sendMessage(commandData.getUser(),
                    "Команда введена неверно! Введите /set_balance <новый баланс>");
            return;
        }

        commandData.getUser().setBalance(amount);
        userRepository.saveUser(session, commandData.getUser());
        commandData.getBot().sendMessage(commandData.getUser(), Message.SET_BALANCE_SUCCESSFULLY
                .replace("{balance}", beautifyDouble(amount)));

    }

    /**
     * Форматирует double в красивую строку.
     * Если число целое, то вернет его без дробной части.
     * Т.е. 1000.0 будет выведено как 1000,
     * а 1000.99 будет выведено как 1000.99
     */
    private String beautifyDouble(double d) {
        if ((int) d == d) {
            return String.valueOf((int) d);
        }
        return String.valueOf(d);
    }
}
