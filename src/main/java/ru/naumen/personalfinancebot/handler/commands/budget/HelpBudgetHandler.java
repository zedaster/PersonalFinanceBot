package ru.naumen.personalfinancebot.handler.commands.budget;

import ru.naumen.personalfinancebot.handler.commands.CommandHandler;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;


/**
 * Класс для обработки команды "/budget_help"
 */
public class HelpBudgetHandler implements CommandHandler {
    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(HandleCommandEvent commandEvent) {
        commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.BUDGET_HELP);
    }
}
