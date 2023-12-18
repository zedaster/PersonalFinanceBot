package ru.naumen.personalfinancebot.handler.command.budget;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;


/**
 * Класс для обработки команды "/budget_help"
 */
public class HelpBudgetHandler implements CommandHandler {
    /**
     * Сообщение, предоставляющее пользователю информацию о доступных командах для пользователя
     */
    private static final String BUDGET_HELP = """
            Доступные команды для работы с бюджетами:
            /budget - показать бюджет за текущий месяц
            /budget_list - запланированный бюджет за последние 12 месяцев
            /budget_list [yyyy - год] -  запланированные бюджеты за определенный год
            /budget_list [mm.yyyy from - месяц от] [mm.year to - месяц до] - запланированные бюджеты за определенные месяца
            /budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы] - планировать бюджет""";

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        commandData.getBot().sendMessage(commandData.getUser(), BUDGET_HELP);
    }
}
