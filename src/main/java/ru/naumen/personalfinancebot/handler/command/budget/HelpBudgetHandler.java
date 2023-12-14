package ru.naumen.personalfinancebot.handler.command.budget;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;


/**
 * Класс для обработки команды "/budget_help"
 */
public class HelpBudgetHandler implements CommandHandler {
    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(CommandData commandData, Session session) {
        commandData.getBot().sendMessage(commandData.getUser(), Message.BUDGET_HELP);
    }
}
