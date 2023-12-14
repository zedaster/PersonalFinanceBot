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
 * Класс для подготовки отчётов в текстовом виде
 */
public class ReportService {
    /**
     * Сообщение о неверно переданной дате (месяц и год) для команды /report_expense
     */
    private static final String INCORRECT_SELF_REPORT_VALUES = """
            Переданы неверные данные месяца и года.
            Дата должна быть передана в виде "MM.YYYY", например, "11.2023".""";

    /**
     * Начало отчета по расходам пользователя
     */
    private static final String SELF_REPORT_MESSAGE = "Подготовил отчёт по вашим расходам за указанный месяц:\n";

    /**
     * Сообщение об отсутствии данных по затратам
     */
    private static final String EXPENSES_NOT_EXIST = "К сожалению, данные по затратам отсутствуют";

    /**
     * Шаблон строки отчета для команды /report_expense
     */
    private static final String EXPENSE_REPORT_PATTERN = "%s: %s руб.\n";

    /**
     * Репозиторий для работы с операциями.
     */
    private final OperationRepository operationRepository;

    private final OutputMonthFormatService monthFormatService;

    private final OutputNumberFormatService numberFormatService;

    public ReportService(OperationRepository operationRepository, OutputMonthFormatService monthFormatService, OutputNumberFormatService numberFormatService) {
        this.operationRepository = operationRepository;
        this.monthFormatService = monthFormatService;
        this.numberFormatService = numberFormatService;
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
            return INCORRECT_SELF_REPORT_VALUES;
        }
        Map<String, Double> categoryPaymentMap = this
                .operationRepository
                .getOperationsSumByType(
                        session,
                        user, yearMonth.getMonth().getValue(), yearMonth.getYear(), CategoryType.EXPENSE
                );
        if (categoryPaymentMap == null) {
            return EXPENSES_NOT_EXIST;
        }
        StringBuilder report = new StringBuilder();
        report.append(SELF_REPORT_MESSAGE);

        for (Map.Entry<String, Double> entry : categoryPaymentMap.entrySet()) {
            report.append(EXPENSE_REPORT_PATTERN.formatted(entry.getKey(), entry.getValue().toString()));
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
                    .replace("{month}", this.monthFormatService.formatRuMonthName(yearMonth.getMonth()))
                    .replace("{year}", String.valueOf(yearMonth.getYear()));
        }

        String emptyContent = Message.EMPTY_LIST_CONTENT;
        String formatIncome = this.numberFormatService.formatDouble(data.get(CategoryType.INCOME), emptyContent);
        String formatExpenses = this.numberFormatService.formatDouble(data.get(CategoryType.EXPENSE), emptyContent);

        return template
                .replace("{income}", formatIncome)
                .replace("{expenses}", formatExpenses);
    }
}
