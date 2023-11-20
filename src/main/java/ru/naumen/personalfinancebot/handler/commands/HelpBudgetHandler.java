package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;


/**
 * Класс для обработки команды "/budget_help"
 */
public class HelpBudgetHandler implements CommandHandler {
    /**
     * Метод, вызываемый при получении команды
     *
     * @param commandEvent
     */
    @Override
    public void handleCommand(HandleCommandEvent commandEvent) {
        commandEvent.getBot().sendMessage(
                commandEvent.getUser(),
                "Доступные команды для работы с бюджетами:\n"
                        + "/budget - показать бюджет за текущий месяц\n"
                        + "/budget_list - запланированный бюджет за последние 12 месяцев\n"
                        + "/budget_list [yyyy - год] -  запланированные бюджеты за определенный год\n"
                        + "/budget_list [mm.yyyy from - месяц от] [mm.year to - месяц до] - запланированные бюджеты за определенные месяца\n"
                        + "/budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы] - планировать бюджет\n"
        );
    }
}
