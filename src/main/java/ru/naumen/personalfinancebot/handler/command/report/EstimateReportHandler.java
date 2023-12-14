package ru.naumen.personalfinancebot.handler.command.report;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.service.DateParseService;
import ru.naumen.personalfinancebot.service.ReportService;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Класс для обработки команды "/estimate_report"
 */
public class EstimateReportHandler implements CommandHandler {
    /**
     * Сервис для парсина аргументов
     */
    private final DateParseService dateParseService;

    /**
     * Сервис для подготовки отчетов
     */
    private final ReportService reportService;

    public EstimateReportHandler(DateParseService dateParseService, ReportService reportService) {
        this.dateParseService = dateParseService;
        this.reportService = reportService;
    }

    @Override
    public void handleCommand(CommandData data, Session session) {
        YearMonth yearMonth;
        boolean isCurrentMonth = false;
        if (data.getArgs().isEmpty()) {
            yearMonth = YearMonth.now();
            isCurrentMonth = true;
        } else if (data.getArgs().size() == 1) {
            try {
                yearMonth = this.dateParseService.parseYearMonth(data.getArgs().get(0));
            } catch (DateTimeParseException e) {
                data.getBot().sendMessage(data.getUser(), Message.INCORRECT_BUDGET_YEAR_MONTH);
                return;
            }
        } else {
            data.getBot().sendMessage(data.getUser(), Message.ESTIMATE_REPORT_INCORRECT_ARGUMENT_COUNT);
            return;
        }

        String report = this.reportService.getEstimateReport(session, yearMonth);
        if (report == null) {
            if (isCurrentMonth) {
                data.getBot().sendMessage(data.getUser(), Message.CURRENT_DATA_NOT_EXISTS);
                return;
            }
            data.getBot().sendMessage(data.getUser(), Message.DATA_NOT_EXISTS);
            return;
        }
        data.getBot().sendMessage(data.getUser(), report);
    }
}
