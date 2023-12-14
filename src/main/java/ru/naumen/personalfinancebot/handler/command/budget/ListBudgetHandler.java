package ru.naumen.personalfinancebot.handler.command.budget;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.repository.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.service.ArgumentParseService;
import ru.naumen.personalfinancebot.service.OutputFormatService;

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
    public void handleCommand(CommandData commandData, Session session) {
        List<String> arguments = commandData.getArgs();
        YearMonth from = YearMonth.now();
        YearMonth to = YearMonth.now();
        String postfixMessage;
        if (arguments.isEmpty()) {
            from = from.minusMonths(12);
            postfixMessage = Message.BUDGET_LIST_TWELVE_MONTHS_POSTFIX;
        } else if (arguments.size() == 1) {
            int year;
            try {
                year = argumentParser.parseYear(arguments.get(0));
            } catch (NumberFormatException e) {
                commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_BUDGET_YEAR_ARG);
                return;
            }
            from = YearMonth.of(year, 1);
            to = YearMonth.of(year, 12);
            postfixMessage = Message.BUDGET_LIST_YEAR_POSTFIX.replace("{year}", String.valueOf(year));
        } else if (arguments.size() == 2) {
            try {
                from = argumentParser.parseYearMonth(arguments.get(0));
                to = argumentParser.parseYearMonth(arguments.get(1));
            } catch (DateTimeParseException e) {
                commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_BUDGET_YEAR_MONTH);
                return;
            }

            long monthsBetween = from.until(to, ChronoUnit.MONTHS) + 1;
            postfixMessage = Message.BUDGET_LIST_RANGE_POSTFIX.replace("{count}",
                    String.valueOf(monthsBetween));
        } else {
            commandData.getBot().sendMessage(commandData.getUser(), Message.INCORRECT_LIST_BUDGET_ENTIRE_ARGS);
            return;
        }

        if (from.isAfter(to)) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.BUDGET_LIST_FROM_IS_AFTER_TO);
            return;
        }

        // Список бюджетов за указанный период;
        List<Budget> budgets = this.budgetRepository.selectBudgetRange(session, commandData.getUser(), from, to);
        if (budgets.isEmpty()) {
            commandData.getBot().sendMessage(commandData.getUser(), Message.BUDGET_LIST_EMPTY);
            return;
        }

        StringBuilder resultReplyMessage = new StringBuilder();
        resultReplyMessage.append(Message.BUDGET_LIST_PREFIX);
        resultReplyMessage.append("\n");
        for (Budget budget : budgets) {
            YearMonth targetYearMonth = budget.getTargetDate();
            double expectIncome = budget.getExpectedSummary(CategoryType.INCOME);
            double expectExpenses = budget.getExpectedSummary(CategoryType.EXPENSE);
            double realIncome = this.operationRepository
                    .getCurrentUserPaymentSummary(session, commandData.getUser(), CategoryType.INCOME, targetYearMonth);
            double realExpenses = this.operationRepository
                    .getCurrentUserPaymentSummary(session, commandData.getUser(), CategoryType.EXPENSE, targetYearMonth);

            resultReplyMessage.append(Message.BUDGET_LIST_ELEMENT
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

        commandData.getBot().sendMessage(commandData.getUser(), resultReplyMessage.toString());
    }
}
