package ru.naumen.personalfinancebot.handler.commands;

import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.services.ArgumentParseService;
import ru.naumen.personalfinancebot.services.ReportService;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Класс для обработки команды "/avg_report"
 */
public class AverageReportHandler implements CommandHandler {
    /**
     * Сервис для парсина аргументов
     */
    private final ArgumentParseService argumentParseService;
    /**
     * Сервис для подготовки отчетов
     */
    private final ReportService reportService;

    /**
     * @param argumentParseService Сервис для парсина аргументов
     * @param reportService Сервис для подготовки отчетов
     */
    public AverageReportHandler(ArgumentParseService argumentParseService, ReportService reportService) {
        this.argumentParseService = argumentParseService;
        this.reportService = reportService;
    }

    /**
     * Метод, вызываемый при получении команды
     *
     * @param commandEvent
     */
    @Override
    public void handleCommand(HandleCommandEvent commandEvent) {
        YearMonth yearMonth;
        if (commandEvent.getArgs().isEmpty()) {
            yearMonth = YearMonth.now();
        } else if (commandEvent.getArgs().size() == 1) {
            try {
                yearMonth = this.argumentParseService.parseYearMonth(commandEvent.getArgs().get(0));
            } catch (DateTimeParseException e) {
                commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.INCORRECT_BUDGET_YEAR_MONTH);
                return;
            }
        } else {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.AVG_REPORT_INCORRECT_ARGUMENT_COUNT);
            return;
        }
        String report = this.reportService.getAverageReport(yearMonth);
        if (report == null) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.DATA_NOT_EXISTS);
            return;
        }
        commandEvent.getBot().sendMessage(commandEvent.getUser(), report);
    }
}
