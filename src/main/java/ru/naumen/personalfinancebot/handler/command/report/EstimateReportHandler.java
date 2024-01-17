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
     * Сервис для парсинга даты из аргументов
     */
    private final DateParseService dateParseService;

    /**
     * Сервис для подготовки отчетов
     */
    private final ReportService reportService;

    /**
     * Сообщение, выводимое при недопустимом количестве аргументов
     */
    private static final String INCORRECT_ARGUMENT_COUNT = """
            Команда "/estimate_report" не принимает аргументов, либо принимает Месяц и Год в формате "MM.YYYY".
            Например, "/estimate_report" или "/estimate_report 12.2023".""";

    public EstimateReportHandler(DateParseService dateParseService, ReportService reportService) {
        this.dateParseService = dateParseService;
        this.reportService = reportService;
    }

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        YearMonth yearMonth;
        try {
            yearMonth = this.dateParseService.parseYearMonthArgs(commandData.getArgs());
        } catch (DateTimeParseException exception) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_YEAR_MONTH_FORMAT);
            return;
        } catch (IllegalArgumentException exception) {
            commandData.getBot().sendMessage(commandData.getUser(), INCORRECT_ARGUMENT_COUNT);
            return;
        }

        String report = this.reportService.getEstimateReport(session, yearMonth);
        if (report == null) {
            if (commandData.getArgs().isEmpty()) {
                commandData.getBot().sendMessage(commandData.getUser(), Message.CURRENT_DATA_NOT_EXISTS);
                return;
            }
            commandData.getBot().sendMessage(commandData.getUser(), Message.DATA_NOT_EXISTS);
            return;
        }
        commandData.getBot().sendMessage(commandData.getUser(), report);
    }
}
