package ru.naumen.personalfinancebot.handler.commands.budget;

import ru.naumen.personalfinancebot.handler.commands.CommandHandler;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.models.Budget;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.repositories.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.services.ArgumentParseService;
import ru.naumen.personalfinancebot.services.OutputFormatService;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Обработчик для команды "/budget_list"
 */
public class ListBudgetHandler implements CommandHandler {
    /**
     * Репозиторий для работы с бюджетом
     */
    private final BudgetRepository budgetRepository;

    /**
     * Репозиторий для работы с операциями
     */
    private final OperationRepository operationRepository;

    /**
     * Сервис, который парсит аргументы
     */
    private final ArgumentParseService argumentParser;

    /**
     * Сервис, который приводит данные для вывода к нужному формату
     */
    private final OutputFormatService outputFormatter;

    /**
     * @param budgetRepository    Репозиторий для работы с бюджетом
     * @param operationRepository Репозиторий для работы с операциями
     * @param argumentParser      Сервис, который парсит аргументы
     * @param outputFormatter     Сервис, который приводит данные для вывода к нужному формату
     */
    public ListBudgetHandler(BudgetRepository budgetRepository, OperationRepository operationRepository, ArgumentParseService argumentParser, OutputFormatService outputFormatter) {
        this.budgetRepository = budgetRepository;
        this.operationRepository = operationRepository;
        this.argumentParser = argumentParser;
        this.outputFormatter = outputFormatter;
    }

    /**
     * Метод, вызываемый при получении команды
     */
    @Override
    public void handleCommand(HandleCommandEvent commandEvent) {
        List<String> arguments = commandEvent.getArgs();
        YearMonth from = YearMonth.now();
        YearMonth to = YearMonth.now();
        String postfixMessage;
        if (arguments.isEmpty()) {
            from = from.minusMonths(12);
            postfixMessage = StaticMessages.BUDGET_LIST_TWELVE_MONTHS_POSTFIX;
        } else if (arguments.size() == 1) {
            int year;
            try {
                year = argumentParser.parseYear(arguments.get(0));
            } catch (NumberFormatException e) {
                commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.INCORRECT_BUDGET_YEAR_ARG);
                return;
            }
            from = YearMonth.of(year, 1);
            to = YearMonth.of(year, 12);
            postfixMessage = StaticMessages.BUDGET_LIST_YEAR_POSTFIX.replace("{year}", String.valueOf(year));
        } else if (arguments.size() == 2) {
            try {
                from = argumentParser.parseYearMonth(arguments.get(0));
                to = argumentParser.parseYearMonth(arguments.get(1));
            } catch (DateTimeParseException e) {
                commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.INCORRECT_BUDGET_YEAR_MONTH);
                return;
            }

            long monthsBetween = from.until(to, ChronoUnit.MONTHS) + 1;
            postfixMessage = StaticMessages.BUDGET_LIST_RANGE_POSTFIX.replace("{count}",
                    String.valueOf(monthsBetween));
        } else {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.INCORRECT_LIST_BUDGET_ENTIRE_ARGS);
            return;
        }

        if (from.isAfter(to)) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.BUDGET_LIST_FROM_IS_AFTER_TO);
            return;
        }

        // Список бюджетов за указанный период;
        List<Budget> budgets = this.budgetRepository.selectBudgetRange(commandEvent.getUser(), from, to);
        if (budgets.isEmpty()) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.BUDGET_LIST_EMPTY);
            return;
        }

        StringBuilder resultReplyMessage = new StringBuilder();
        resultReplyMessage.append(StaticMessages.BUDGET_LIST_PREFIX);
        resultReplyMessage.append("\n");
        for (Budget budget : budgets) {
            YearMonth targetYearMonth = budget.getTargetDate();
            double expectIncome = budget.getExpectedSummary(CategoryType.INCOME);
            double expectExpenses = budget.getExpectedSummary(CategoryType.EXPENSE);
            double realIncome = this.operationRepository
                    .getCurrentUserPaymentSummary(commandEvent.getUser(), CategoryType.INCOME, targetYearMonth);
            double realExpenses = this.operationRepository
                    .getCurrentUserPaymentSummary(commandEvent.getUser(), CategoryType.EXPENSE, targetYearMonth);

            resultReplyMessage.append(StaticMessages.BUDGET_LIST_ELEMENT
                    .replace("{month}", outputFormatter.formatRuMonthName(targetYearMonth.getMonth()))
                    .replace("{year}", String.valueOf(targetYearMonth.getYear()))
                    .replace("{expect_income}", outputFormatter.formatDouble(expectIncome))
                    .replace("{expect_expenses}", outputFormatter.formatDouble(expectExpenses))
                    .replace("{real_income}", outputFormatter.formatDouble(realIncome))
                    .replace("{real_expenses}", outputFormatter.formatDouble(realExpenses))

            );
            resultReplyMessage.append("\n\n");
        }
        resultReplyMessage.append(postfixMessage);

        commandEvent.getBot().sendMessage(commandEvent.getUser(), resultReplyMessage.toString());
    }
}
