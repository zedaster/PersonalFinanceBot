package ru.naumen.personalfinancebot.handler.command.report;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.service.ReportService;

/**
 * Обработчик для команды "/report_expense"
 *
 * @author Aleksandr Kornilov
 */
public class ReportExpensesHandler implements CommandHandler {
    private final ReportService reportService;

    public ReportExpensesHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Метод, вызываемый при получении команды "/report_expense"
     */
    @Override
    public void handleCommand(CommandData commandData, Session session) {
        if (commandData.getArgs().size() != 1) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_SELF_REPORT_ARGS);
            return;
        }
        String report = this.reportService.getExpenseReport(session, commandData.getUser(), commandData.getArgs().get(0));
        commandData.getBot().sendMessage(commandData.getUser(), report);
    }
}
