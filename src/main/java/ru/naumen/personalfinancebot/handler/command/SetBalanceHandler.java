package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.repository.user.UserRepository;
import ru.naumen.personalfinancebot.service.ArgumentParseService;
import ru.naumen.personalfinancebot.service.OutputFormatService;

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
        commandData.getBot().sendMessage(
                commandData.getUser(),
                Message.SET_BALANCE_SUCCESSFULLY.formatted(this.outputFormatter.formatDouble(amount))
        );
    }
}
