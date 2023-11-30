package ru.naumen.personalfinancebot.services;

import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Класс для подготовки отчётов по расходам
 */
public class ReportService {
    private final OperationRepository operationRepository;
    private final OutputFormatService outputFormatService;

    public ReportService(OperationRepository operationRepository, OutputFormatService outputFormatService) {
        this.operationRepository = operationRepository;
        this.outputFormatService = outputFormatService;
    }

    /**
     * Возвращет linkedHashMap с категорией расходов и суммой затрат по категории
     *
     * @param user Пользователь, для которого надо вернуть отчёт
     * @param args Аргументы, переданные вместе с командой
     * @return "Словарь" с категориями и затратоми
     */
    public Map<String, Double> getExpenseReport(User user, List<String> args) {
        int month = Integer.parseInt(args.get(0));
        int year = Integer.parseInt(args.get(1));
        return this.operationRepository.getOperationsSumByType(user, month, year, CategoryType.EXPENSE);
    }

    /**
     * Подготавливает отчёт по средним стандартным категория за указанный период
     *
     * @param yearMonth Период [MM.YYYY]
     * @return Отчёт в виде строки
     */
    public String getAverageReport(YearMonth yearMonth) {
        Map<String, Double> data = this.operationRepository.getSummaryByStandardCategory(yearMonth);
        if (data == null) {
            return null;
        }
        StringBuilder report = new StringBuilder();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            report.append(StaticMessages.CATEGORY_PAYMENT_REPORT_PATTERN
                    .replace("{category}", entry.getKey())
                    .replace("{payment}", this.outputFormatService.formatDouble(entry.getValue()))
            );
        }
        return StaticMessages.AVG_REPORT_HEADER
                .replace("{ruMonth}", this.outputFormatService.formatRuMonthName(yearMonth.getMonth()))
                .replace("{year}", String.valueOf(yearMonth.getYear()))
                + report;
    }
}
