package ru.naumen.personalfinancebot.handler.command.budget;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.repository.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.service.DateParseService;
import ru.naumen.personalfinancebot.service.OutputMonthFormatService;
import ru.naumen.personalfinancebot.service.OutputNumberFormatService;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Обработчик для команды "/budget_list"
 */
public class ListBudgetHandler implements CommandHandler {
    /**
     * Сообщение о некорректно переданном годе для создания бюджета.
     */
    private static final String INCORRECT_BUDGET_YEAR_ARG = "Год введен неверно! Дальше 3000 года не стоит планировать.";

    /**
     * Сообщение о неверно введенной команде /budget_list
     */
    private static final String INCORRECT_LIST_BUDGET_ENTIRE_ARGS = """
            Неверно введена команда! Введите
            или /budget_list - вывод бюджетов за 12 месяцев (текущий + предыдущие),
            или /budget_list [год] - вывод бюджетов за определенный год,
            или /budget_list [mm.yyyy - месяц.год] [mm.yyyy - месяц.год] - вывод бюджетов за указанный промежуток.""";

    /**
     * Сообщение об ошибке если передана дата начала, которая позднее даты конца
     */
    private static final String BUDGET_LIST_FROM_IS_AFTER_TO = "Дата начала не может быть позднее даты конца периода!";

    /**
     * Префикс для списка запланированных бюджетов.
     */
    private static final String BUDGET_LIST_PREFIX = "Ваши запланированные доходы и расходы по месяцам:";

    /**
     * Шаблон для вывода бюджета за конкретный год и месяц
     */
    private static final String BUDGET_LIST_ELEMENT = """
            %s %s:
            Ожидание: + %s | - %s
            Реальность: + %s | - %s""";

    /**
     * Постфикс для сообщения пользователю при выводе списка бюджетов за n-ое кол-во месяцев
     */
    private static final String BUDGET_LIST_RANGE_POSTFIX = "Данные показаны за %s месяц(-ев).";

    /**
     * Постфикс для сообщения пользователю при выводе списка бюджетов за конкретный год
     */
    private static final String BUDGET_LIST_YEAR_POSTFIX = "Данные показаны за %s год.";

    /**
     * Сообщение для вывода списка бюджетов за последние 12 месяцев.
     */
    private static final String BUDGET_LIST_TWELVE_MONTHS_POSTFIX = "Данные показаны за последние 12 месяцев. " +
            "Чтобы посмотреть данные, например, за 2022, введите /budget_list 2022.\n" +
            "Для показа данных по определенным месяцам, например, с ноября 2022 по январь 2023 введите " +
            "/budget_list 10.2022 01.2023";

    /**
     * Сообщение об отсутствии бюджетов за указанный пользователем период
     */
    private static final String BUDGET_LIST_EMPTY = "У вас не было бюджетов за этот период. Для создания бюджета " +
            "введите /budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы]";

    /**
     * Репозиторий для работы с бюджетом
     */
    private final BudgetRepository budgetRepository;

    /**
     * Репозиторий для работы с операциями
     */
    private final OperationRepository operationRepository;

    /**
     * Сервис, который парсит дату
     */
    private final DateParseService dateParseService;

    /**
     * Сервис, который форматирует числа
     */
    private final OutputNumberFormatService numberFormatService;

    /**
     * Сервис, который форматирует месяц к русскому названию
     */
    private final OutputMonthFormatService monthFormatService;

    /**
     * @param budgetRepository    Репозиторий для работы с бюджетом
     * @param operationRepository Репозиторий для работы с операциями
     * @param dateParseService    Сервис, который парсит дату
     * @param numberFormatService Сервис, который форматирует числа
     * @param monthFormatService  Сервис, который форматирует месяц к русскому названию
     */
    public ListBudgetHandler(BudgetRepository budgetRepository, OperationRepository operationRepository, DateParseService dateParseService, OutputNumberFormatService numberFormatService, OutputMonthFormatService monthFormatService) {
        this.budgetRepository = budgetRepository;
        this.operationRepository = operationRepository;
        this.dateParseService = dateParseService;
        this.numberFormatService = numberFormatService;
        this.monthFormatService = monthFormatService;
    }

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        List<String> arguments = commandData.getArgs();
        YearMonth from = YearMonth.now();
        YearMonth to = YearMonth.now();
        String postfixMessage;
        if (arguments.isEmpty()) {
            from = from.minusMonths(12);
            postfixMessage = BUDGET_LIST_TWELVE_MONTHS_POSTFIX;
        } else if (arguments.size() == 1) {
            int year;
            try {
                year = dateParseService.parseYear(arguments.get(0));
            } catch (NumberFormatException e) {
                commandData.getBot().sendMessage(commandData.getUser(), INCORRECT_BUDGET_YEAR_ARG);
                return;
            }
            from = YearMonth.of(year, 1);
            to = YearMonth.of(year, 12);
            postfixMessage = BUDGET_LIST_YEAR_POSTFIX.formatted(String.valueOf(year));
        } else if (arguments.size() == 2) {
            try {
                from = dateParseService.parseYearMonth(arguments.get(0));
                to = dateParseService.parseYearMonth(arguments.get(1));
            } catch (DateTimeParseException e) {
                commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_YEAR_MONTH_FORMAT);
                return;
            }

            long monthsBetween = from.until(to, ChronoUnit.MONTHS) + 1;
            postfixMessage = BUDGET_LIST_RANGE_POSTFIX.formatted(String.valueOf(monthsBetween));
        } else {
            commandData.getBot().sendMessage(commandData.getUser(), INCORRECT_LIST_BUDGET_ENTIRE_ARGS);
            return;
        }

        if (from.isAfter(to)) {
            commandData.getBot().sendMessage(commandData.getUser(), BUDGET_LIST_FROM_IS_AFTER_TO);
            return;
        }

        // Список бюджетов за указанный период;
        List<Budget> budgets = this.budgetRepository.selectBudgetRange(session, commandData.getUser(), from, to);
        if (budgets.isEmpty()) {
            commandData.getBot().sendMessage(commandData.getUser(), BUDGET_LIST_EMPTY);
            return;
        }

        StringBuilder resultReplyMessage = new StringBuilder();
        resultReplyMessage.append(BUDGET_LIST_PREFIX);
        resultReplyMessage.append("\n");
        for (Budget budget : budgets) {
            YearMonth targetYearMonth = budget.getTargetDate();
            double expectIncome = budget.getIncome();
            double expectExpenses = budget.getExpense();
            double realIncome = this.operationRepository
                    .getCurrentUserPaymentSummary(session, commandData.getUser(), CategoryType.INCOME, targetYearMonth);
            double realExpenses = this.operationRepository
                    .getCurrentUserPaymentSummary(session, commandData.getUser(), CategoryType.EXPENSE, targetYearMonth);

            resultReplyMessage.append(BUDGET_LIST_ELEMENT.formatted(
                    monthFormatService.formatRuMonthName(targetYearMonth.getMonth()),
                    String.valueOf(targetYearMonth.getYear()),
                    numberFormatService.formatDouble(expectIncome),
                    numberFormatService.formatDouble(expectExpenses),
                    numberFormatService.formatDouble(realIncome),
                    numberFormatService.formatDouble(realExpenses))
            );
            resultReplyMessage.append("\n\n");
        }
        resultReplyMessage.append(postfixMessage);

        commandData.getBot().sendMessage(commandData.getUser(), resultReplyMessage.toString());
    }
}
