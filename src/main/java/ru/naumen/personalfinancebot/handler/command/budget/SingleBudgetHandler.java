package ru.naumen.personalfinancebot.handler.command.budget;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.repository.budget.BudgetRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.service.OutputMonthFormatService;
import ru.naumen.personalfinancebot.service.OutputNumberFormatService;

import java.time.YearMonth;
import java.util.Optional;

/**
 * Класс для обработки команды "/budget"
 */
public class SingleBudgetHandler implements CommandHandler {
    /**
     * Сообщение об отсутствии бюджета за конкретную дату
     */
    private static final String CURRENT_BUDGET_NOT_EXISTS = "Бюджет на %s %s отсутствует";

    /**
     * Шаблон сообщения для вывода текущего бюджета
     */
    private final String CURRENT_BUDGET = """
            Бюджет на %s %s:
            Ожидаемые доходы: %s
            Ожидаемые расходы: %s
            Текущие доходы: %s
            Текущие расходы: %s
            Текущий баланс: %s
            Нужно еще заработать: %s
            Еще осталось на траты: %s""";

    /**
     * Репозиторий для работы с бюджетом
     */
    private final BudgetRepository budgetRepository;

    /**
     * Репозиторий для работы с операциями
     */
    private final OperationRepository operationRepository;

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
     * @param numberFormatService Сервис, который форматирует числа
     * @param monthFormatService Сервис, который форматирует месяц к русскому названию
     */
    public SingleBudgetHandler(BudgetRepository budgetRepository, OperationRepository operationRepository, OutputNumberFormatService numberFormatService, OutputMonthFormatService monthFormatService) {
        this.budgetRepository = budgetRepository;
        this.operationRepository = operationRepository;
        this.numberFormatService = numberFormatService;
        this.monthFormatService = monthFormatService;
    }

    @Override
    public void handleCommand(CommandData commandData, Session session) {
        YearMonth currentMonthYear = YearMonth.now();
        Optional<Budget> budget = this.budgetRepository.getBudget(session, commandData.getUser(), YearMonth.now());
        if (budget.isEmpty()) {
            commandData.getBot().sendMessage(
                    commandData.getUser(),
                    CURRENT_BUDGET_NOT_EXISTS.formatted(
                            monthFormatService.formatRuMonthName(currentMonthYear.getMonth()),
                            String.valueOf(currentMonthYear.getYear()))
            );
            return;
        }

        double expectIncome = budget.get().getIncome();
        double expectExpenses = budget.get().getExpense();
        double realIncome = this.operationRepository.getCurrentUserPaymentSummary(
                session,
                commandData.getUser(), CategoryType.INCOME, currentMonthYear
        );
        double realExpenses = this.operationRepository.getCurrentUserPaymentSummary(
                session,
                commandData.getUser(), CategoryType.EXPENSE, currentMonthYear
        );
        double incomeLeft = Math.max(0, expectIncome - realIncome);
        double expensesLeft = Math.max(0, expectExpenses - realExpenses);
        double balance = commandData.getUser().getBalance();

        commandData.getBot().sendMessage(
                commandData.getUser(),
                CURRENT_BUDGET.formatted(
                        monthFormatService.formatRuMonthName(currentMonthYear.getMonth()),
                        String.valueOf(currentMonthYear.getYear()),
                        numberFormatService.formatDouble(expectIncome),
                        numberFormatService.formatDouble(expectExpenses),
                        numberFormatService.formatDouble(realIncome),
                        numberFormatService.formatDouble(realExpenses),
                        numberFormatService.formatDouble(balance),
                        numberFormatService.formatDouble(incomeLeft),
                        numberFormatService.formatDouble(expensesLeft)));
    }
}
