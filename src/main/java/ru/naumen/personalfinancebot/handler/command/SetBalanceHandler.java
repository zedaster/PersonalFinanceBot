package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.repository.user.UserRepository;
import ru.naumen.personalfinancebot.service.NumberParseService;
import ru.naumen.personalfinancebot.service.OutputNumberFormatService;

/**
 * Обработчик команды для установки баланса
 *
 * @author Sergey Kazantsev
 */
public class SetBalanceHandler implements CommandHandler {
    /**
     * Сообщение для команды /set_balance
     */
    private static final String SET_BALANCE_SUCCESSFULLY = "Ваш баланс изменен. Теперь он составляет %s";

    /**
     * Сервис, который парсит числа
     */
    private final NumberParseService numberParseService;

    /**
     * Сервис, который приводит числа для вывода к нужному формату
     */
    private final OutputNumberFormatService numberFormatService;

    /**
     * Хранилище пользователей
     */
    private final UserRepository userRepository;

    public SetBalanceHandler(NumberParseService numberParseService, OutputNumberFormatService numberFormatService,
                             UserRepository userRepository) {
        this.numberParseService = numberParseService;
        this.numberFormatService = numberFormatService;
        this.userRepository = userRepository;
    }

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        double amount;
        try {
            amount = numberParseService.parseBalance(commandData.getArgs());
        } catch (IllegalArgumentException e) {
            commandData.getBot().sendMessage(commandData.getUser(),
                    "Команда введена неверно! Введите /set_balance <новый баланс>");
            return;
        }

        commandData.getUser().setBalance(amount);
        userRepository.saveUser(session, commandData.getUser());
        commandData.getBot().sendMessage(
                commandData.getUser(),
                SET_BALANCE_SUCCESSFULLY.formatted(this.numberFormatService.formatDouble(amount))
        );
    }
}
