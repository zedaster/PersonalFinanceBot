package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.services.ReportService;

import java.util.List;
import java.util.Map;

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
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(HandleCommandEvent event) {
        if (event.getArgs().size() != 1) {
            event.getBot().sendMessage(event.getUser(), StaticMessages.INCORRECT_SELF_REPORT_ARGS);
            return;
        }
        List<String> parsedArgs = List.of(event.getArgs().get(0).split("\\."));
        if (!isCorrectReportArgs(parsedArgs.get(0), parsedArgs.get(1))) {
            event.getBot().sendMessage(event.getUser(), StaticMessages.INCORRECT_SELF_REPORT_VALUES);
            return;
        }

        Map<String, Double> categoryPaymentMap = this.reportService.getExpenseReport(event.getUser(), parsedArgs);
        if (categoryPaymentMap == null) {
            event.getBot().sendMessage(event.getUser(), "К сожалению, данные по затратам отсутствуют");
            return;
        }
        StringBuilder message = new StringBuilder();
        message.append(StaticMessages.SELF_REPORT_MESSAGE);
        for (Map.Entry<String, Double> entry : categoryPaymentMap.entrySet()) {
            message.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("руб.\n");
        }
        event.getBot().sendMessage(event.getUser(), message.toString());

    }

    /**
     * Проверяет корректность аргументов для команды /report_expense
     *
     * @param month введенный месяц
     * @param year  введенный год
     * @return true при корректных данных, иначе - false
     */
    private boolean isCorrectReportArgs(String month, String year) {
        int _month = Integer.parseInt(month);
        if (_month < 1 || _month > 12) {
            return false;
        }

        return year.length() == 4 && Integer.parseInt(year) >= 0;
    }
}
