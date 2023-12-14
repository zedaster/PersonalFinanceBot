package ru.naumen.personalfinancebot.service;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
     * Возвращает Отчёт в виде строки, где содержится сумма затрат на каждую категорию пользователя.
     *
     * @param user Пользователь, для которого надо вернуть отчёт
     * @param args Аргументы, переданные вместе с командой
     * @return "Словарь" с категориями и затратоми
     */
    public String getExpenseReport(Session session, User user, String args) {
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(args, DateTimeFormatter.ofPattern("MM.yyyy"));
        } catch (Exception exception) {
            return Message.INCORRECT_SELF_REPORT_VALUES;
        }
        Map<String, Double> categoryPaymentMap = this
                .operationRepository
                .getOperationsSumByType(
                        session,
                        user, yearMonth.getMonth().getValue(), yearMonth.getYear(), CategoryType.EXPENSE
                );
        if (categoryPaymentMap == null) {
            return Message.EXPENSES_NOT_EXIST;
        }
        StringBuilder report = new StringBuilder();
        report.append(Message.SELF_REPORT_MESSAGE);

        for (Map.Entry<String, Double> entry : categoryPaymentMap.entrySet()) {
             report.append(Message.EXPENSE_REPORT_PATTERN
                     .replace("{category}", entry.getKey())
                     .replace("{payment}", entry.getValue().toString())
             );
        }
        return report.toString();
    }

    /**
     * Подготавливает отчёт по средним стандартным категориям за указанный период.
     * Вернет null, если нет данных
     *
     * @param yearMonth Период [MM.YYYY]
     * @return Отчёт в виде строки
     */
    public String getEstimateReport(Session session, YearMonth yearMonth) {
        Map<CategoryType, Double> data = this.operationRepository.getEstimateSummary(session, yearMonth);
        if (data == null) {
            return null;
        }

        String template;
        if (yearMonth.equals(YearMonth.now())) {
            template = Message.ESTIMATE_REPORT_CURRENT;
        } else {
            template = Message.ESTIMATE_REPORT_DATED
                    .replace("{month}", this.outputFormatService.formatRuMonthName(yearMonth.getMonth()))
                    .replace("{year}", String.valueOf(yearMonth.getYear()));
        }

        String emptyContent = Message.EMPTY_LIST_CONTENT;
        String formatIncome = this.outputFormatService.formatDouble(data.get(CategoryType.INCOME), emptyContent);
        String formatExpenses = this.outputFormatService.formatDouble(data.get(CategoryType.EXPENSE), emptyContent);

        return template
                .replace("{income}", formatIncome)
                .replace("{expenses}", formatExpenses);
    }
}
