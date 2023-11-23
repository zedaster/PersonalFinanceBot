package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;
import ru.naumen.personalfinancebot.services.ArgumentParseService;
import ru.naumen.personalfinancebot.services.OutputFormatService;

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
     * Сервис, который приводит данные для вывода к нужному формату
     */
    private final OutputFormatService outputFormatter;

    /**
     * Хранилище пользователей
     */
    private final UserRepository userRepository;

    public SetBalanceHandler(ArgumentParseService argumentParser, OutputFormatService outputFormatter,
                             UserRepository userRepository) {
        this.argumentParser = argumentParser;
        this.outputFormatter = outputFormatter;
        this.userRepository = userRepository;
    }

    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(HandleCommandEvent event) {
        double amount;
        try {
            amount = argumentParser.parseBalance(event.getArgs());
        } catch (IllegalArgumentException e) {
            event.getBot().sendMessage(event.getUser(),
                    "Команда введена неверно! Введите /set_balance <новый баланс>");
            return;
        }

        event.getUser().setBalance(amount);
        userRepository.saveUser(event.getUser());
        event.getBot().sendMessage(event.getUser(), StaticMessages.SET_BALANCE_SUCCESSFULLY
                .replace("{balance}", outputFormatter.formatDouble(amount)));
    }
}
