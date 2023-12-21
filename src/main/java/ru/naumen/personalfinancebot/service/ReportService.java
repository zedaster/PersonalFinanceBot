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
     * Заголовок отчёта по средним расходам/доходам пользователей по стандартным категориям
     */
    private static final String AVG_REPORT_HEADER = "Подготовил отчет по стандартным категориям со всех пользователей за %s %d:\n";

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

    private static final String ESTIMATE_REPORT_CURRENT = """
            Подготовил отчет по средним доходам и расходам пользователей за текущий месяц:
            Расходы: %s
            Доходы: %s""";

    private static final String ESTIMATE_REPORT_DATED = """
            Подготовил отчет по средним доходам и расходам пользователей за %s %d:
            Расходы: %s
            Доходы: %s""";

    /**
     * Репозиторий для работы с операциями
     */
    private final OperationRepository operationRepository;

    /**
     * Сервис для форматирования названия месяца
     */
    private final OutputMonthFormatService monthFormatService;

    /**
     * Сервис для форматирования чисел для вывода
     */
    private final OutputNumberFormatService numberFormatService;

    public ReportService(OperationRepository operationRepository,
                         OutputMonthFormatService monthFormatService,
                         OutputNumberFormatService numberFormatService) {
        this.operationRepository = operationRepository;
        this.monthFormatService = monthFormatService;
        this.numberFormatService = numberFormatService;
    }

    /**
     * Возвращает Отчёт в виде строки, где содержится сумма затрат на каждую категорию пользователя.
     *
     * @param user Пользователь, для которого надо вернуть отчёт
     * @param args Аргументы, переданные вместе с командой
     * @return "Словарь" с категориями и затратами
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

        String emptyContent = Message.EMPTY_LIST_CONTENT;
        String formatExpenses = this.numberFormatService.formatDouble(data.get(CategoryType.EXPENSE), emptyContent);
        String formatIncome = this.numberFormatService.formatDouble(data.get(CategoryType.INCOME), emptyContent);

        if (yearMonth.equals(YearMonth.now())) {
            return ESTIMATE_REPORT_CURRENT.formatted(formatExpenses, formatIncome);
        }

        String monthTitle = this.monthFormatService.formatRuMonthName(yearMonth.getMonth());
        return ESTIMATE_REPORT_DATED.formatted(monthTitle, yearMonth.getYear(), formatExpenses, formatIncome);
    }

    /**
     * Подготавливает отчёт по средним стандартным категориям за указанный период
     *
     * @param session   Сессия
     * @param yearMonth Период
     * @return Отчёт в строковом виде
     */
    public String getAverageReport(Session session, YearMonth yearMonth) {
        Map<String, Double> data = this.operationRepository.getAverageSummaryByStandardCategory(session, yearMonth);
        if (data == null) {
            return null;
        }
        StringBuilder report = new StringBuilder();
        report.append(AVG_REPORT_HEADER.formatted(
                this.monthFormatService.formatRuMonthName(yearMonth.getMonth()),
                yearMonth.getYear()));

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            String categoryPayment = EXPENSE_REPORT_PATTERN.formatted(
                    entry.getKey(), this.numberFormatService.formatDouble(entry.getValue()));
            report.append(categoryPayment);
        }
        return report.toString();
    }
}
