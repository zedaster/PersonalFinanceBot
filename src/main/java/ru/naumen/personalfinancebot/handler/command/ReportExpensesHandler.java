package ru.naumen.personalfinancebot.handler.command;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.service.ReportService;

/**
 * Обработчик для команды "/report_expense"
 *
 * @author Aleksandr Kornilov
 */
public class ReportExpensesHandler implements CommandHandler {
    /**
     * Сообщение о неверно переданном количестве аргументов для команды /report_expense.
     */
    private static final String INCORRECT_SELF_REPORT_ARGS =
            "Команда /report_expense принимает 1 аргумент [mm.yyyy], например \"/report_expense 11.2023\"";

    /**
     * Сервис для составления отчета в строковом виде
     */
    private final ReportService reportService;

    public ReportExpensesHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        if (commandData.getArgs().size() != 1) {
            commandData.getBot().sendMessage(commandData.getUser(), INCORRECT_SELF_REPORT_ARGS);
            return;
        }
        String report = this.reportService.getExpenseReport(session, commandData.getUser(), commandData.getArgs().get(0));
        commandData.getBot().sendMessage(commandData.getUser(), report);
    }
}
