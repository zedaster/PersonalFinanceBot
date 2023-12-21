package ru.naumen.personalfinancebot.handler.command.report;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.service.DateParseService;
import ru.naumen.personalfinancebot.service.ReportService;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

public class AverageReportHandler implements CommandHandler {
    /**
     * Сообщение о неверно переданном количестве аргументов
     */
    private final static String INCORRECT_ARGUMENT_COUNT = """
            Команда "/avg_report" не принимает аргументы, либо принимает Месяц и Год в формате "MM.YYYY"
            Например, "/avg_report" или "/avg_report 12.2023".""";

    /**
     * Сервис для парсинга даты
     */
    private final DateParseService dateParseService;

    /**
     * Сервис, который подготавливает отчёты
     */
    private final ReportService reportService;

    /**
     * @param dateParseService Сервис для парсинга даты
     * @param reportService    Сервис, который подготавливает отчёты
     */
    public AverageReportHandler(DateParseService dateParseService, ReportService reportService) {
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

        String report = this.reportService.getAverageReport(session, yearMonth);
        if (report == null) {
            commandData.getBot().sendMessage(
                    commandData.getUser(),
                    commandData.getArgs().isEmpty()
                            ? Message.CURRENT_DATA_NOT_EXISTS
                            : Message.DATA_NOT_EXISTS
            );
            return;
        }
        commandData.getBot().sendMessage(commandData.getUser(), report);
    }
}
