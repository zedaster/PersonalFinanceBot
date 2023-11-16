package ru.naumen.personalfinancebot.handler;

import com.sun.istack.Nullable;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.messages.StaticMessages;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;
import ru.naumen.personalfinancebot.services.ReportService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Обработчик операций для бота "Персональный финансовый трекер"
 */
public class FinanceBotHandler implements BotHandler {
    /**
     * Коллекция, которая хранит обработчики для каждой команды
     */
    private final Map<String, Consumer<HandleCommandEvent>> handlers;

    private final UserRepository userRepository;
    private final OperationRepository operationRepository;
    private final CategoryRepository categoryRepository;

    public FinanceBotHandler(
            UserRepository userRepository,
            OperationRepository operationRepository,
            CategoryRepository categoryRepository
    ) {
        this.handlers = new HashMap<>();
        this.handlers.put("start", this::handleStartCommand);
        this.handlers.put("set_balance", this::handleSetBalance);
        this.handlers.put("add_expense", this::handleAddExpense);
        this.handlers.put("add_income", this::handleAddIncome);
        this.handlers.put("add_income_category", this::handleAddIncomeCategory);
        this.handlers.put("add_expense_category", this::handleAddExpenseCategory);
        this.handlers.put("remove_income_category", this::handleRemoveIncomeCategory);
        this.handlers.put("remove_expense_category", this::handleRemoveExpenseCategory);
        this.handlers.put("list_categories", this::handleListCategories);
        this.handlers.put("list_income_categories", this::handleListIncomeCategories);
        this.handlers.put("list_expense_categories", this::handleListExpenseCategories);
        this.handlers.put("report_expense", this::handleReportExpense);

        this.operationRepository = operationRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Вызывается при получении какой-либо команды от пользователя
     */
    @Override
    public void handleCommand(HandleCommandEvent event) {
        Consumer<HandleCommandEvent> handler = this.handlers.get(event.getCommandName().toLowerCase());
        if (handler != null) {
            handler.accept(event);
        } else {
            event.getBot().sendMessage(event.getUser(), StaticMessages.COMMAND_NOT_FOUND);
        }
    }

    /**
     * Исполняется при вызове команды /start
     */
    private void handleStartCommand(HandleCommandEvent event) {
        event.getBot().sendMessage(event.getUser(), StaticMessages.WELCOME_MESSAGE);
    }

    /**
     * Команда для установки баланса
     */
    private void handleSetBalance(HandleCommandEvent event) {
        double amount;
        try {
            if (event.getArgs().size() != 1) throw new IllegalArgumentException();
            amount = parseBalanceAmount(event.getArgs().get(0));
        } catch (IllegalArgumentException e) {
            event.getBot().sendMessage(event.getUser(),
                    "Команда введена неверно! Введите /set_balance <новый баланс>");
            return;
        }

        event.getUser().setBalance(amount);
        userRepository.saveUser(event.getUser());
        event.getBot().sendMessage(event.getUser(), StaticMessages.SET_BALANCE_SUCCESSFULLY
                .replace("{balance}", beautifyDouble(amount)));
    }

    /**
     * Команда для добавления пользовательской категории дохода
     */
    private void handleAddIncomeCategory(HandleCommandEvent event) {
        handleAddCategory(CategoryType.INCOME, event);
    }

    /**
     * Команда для добавления пользовательской категории расхода
     */
    private void handleAddExpenseCategory(HandleCommandEvent event) {
        handleAddCategory(CategoryType.EXPENSE, event);
    }

    /**
     * Обработчик команд для добавления пользовательской категории определенного типа
     */
    private void handleAddCategory(CategoryType type, HandleCommandEvent event) {
        String categoryName;
        try {
            categoryName = checkAndReturnSingleCategoryArgument(event.getArgs());
        } catch (IllegalArgumentException ex) {
            event.getBot().sendMessage(event.getUser(), ex.getMessage());
            return;
        }


        String typeLabel = type.getPluralShowLabel();
        try {
            categoryRepository.createUserCategory(event.getUser(), type, categoryName);
        } catch (CategoryRepository.CreatingExistingUserCategoryException e) {
            String responseText = StaticMessages.USER_CATEGORY_ALREADY_EXISTS
                    .replace("{type}", typeLabel)
                    .replace("{name}", categoryName);
            event.getBot().sendMessage(event.getUser(), responseText);
            return;
        } catch (CategoryRepository.CreatingExistingStandardCategoryException e) {
            String responseText = StaticMessages.STANDARD_CATEGORY_ALREADY_EXISTS
                    .replace("{type}", typeLabel)
                    .replace("{name}", categoryName);
            event.getBot().sendMessage(event.getUser(), responseText);
            return;
        }

        String responseText = StaticMessages.USER_CATEGORY_ADDED
                .replace("{type}", typeLabel)
                .replace("{name}", categoryName);
        event.getBot().sendMessage(event.getUser(), responseText);
    }

    /**
     * Команда для удаления пользовательской категории дохода
     */
    private void handleRemoveIncomeCategory(HandleCommandEvent event) {
        handleRemoveCategory(CategoryType.INCOME, event);
    }

    /**
     * Команда для удаления пользовательской категории расхода
     */
    private void handleRemoveExpenseCategory(HandleCommandEvent event) {
        handleRemoveCategory(CategoryType.EXPENSE, event);
    }

    /**
     * Обработчик команд для удаления пользовательской категории определенного типа
     */
    private void handleRemoveCategory(CategoryType type, HandleCommandEvent event) {
        String typeLabel = type.getPluralShowLabel();
        String categoryName;
        try {
            categoryName = checkAndReturnSingleCategoryArgument(event.getArgs());
        } catch (IllegalArgumentException ex) {
            event.getBot().sendMessage(event.getUser(), ex.getMessage());
            return;
        }

        try {
            categoryRepository.removeUserCategoryByName(event.getUser(), type, categoryName);
        } catch (CategoryRepository.RemovingNonExistentCategoryException e) {
            String responseText = StaticMessages.USER_CATEGORY_ALREADY_NOT_EXISTS
                    .replace("{type}", typeLabel)
                    .replace("{name}", categoryName);
            event.getBot().sendMessage(event.getUser(), responseText);
            return;
        }

        String responseText = StaticMessages.USER_CATEGORY_REMOVED
                .replace("{type}", typeLabel)
                .replace("{name}", categoryName);
        event.getBot().sendMessage(event.getUser(), responseText);
    }

    /**
     * Проверяет, что в аргументах одно единственное название категории, которое введено верно
     *
     * @throws IllegalArgumentException если введено несколько аргументов или название категории неверное. В исключении
     *                                  содержится сообщение {@link StaticMessages#INCORRECT_CATEGORY_ARGUMENT_COUNT} или
     *                                  {@link StaticMessages#INCORRECT_CATEGORY_ARGUMENT_FORMAT}
     */
    private String checkAndReturnSingleCategoryArgument(List<String> args) throws IllegalArgumentException {
        if (args.size() != 1) {
            throw new IllegalArgumentException(StaticMessages.INCORRECT_CATEGORY_ARGUMENT_COUNT);
        }

        String categoryName = beautifyCategoryName(args.get(0));
        if (!isValidCategory(categoryName)) {
            throw new IllegalArgumentException(StaticMessages.INCORRECT_CATEGORY_ARGUMENT_FORMAT);
        }
        return categoryName;
    }

    /**
     * Делает красивым и корректным имя категории.
     * Убирает пробелы в начале и заменяет множественные пробелы посередине на одиночные.
     * Первую букву делает заглавной, остальные - маленькими.
     *
     * @param text Строка для обработки
     * @return Новая строка
     */
    private String beautifyCategoryName(String text) {
        char[] newChars = text
                .trim()
                .replaceAll("\\s{2,}", " ")
                .toLowerCase()
                .toCharArray();
        if (newChars.length == 0) {
            return "";
        }
        newChars[0] = Character.toUpperCase(newChars[0]);
        return String.valueOf(newChars);
    }

    /**
     * Проверяет, соответствует ли название категории правильному формату.
     * Символов должно быть от 1 до 64, каждый должен являться либо буквой в кириллице, латинице, либо цифрой,
     * либо пробелом.
     *
     * @param categoryName Название категории
     * @return true/false в зависимости от валидности названия категории
     */
    private boolean isValidCategory(String categoryName) {
        return categoryName.matches("^[A-Za-zА-Яа-я0-9 ]{1,64}$");
    }

    /**
     * Команда для вывода категорий, доступные пользователю (пользовательские, а также встроенные)
     */
    private void handleListCategories(HandleCommandEvent event) {
        String incomeContent = getListResponse(event.getUser(), CategoryType.INCOME);
        String expenseContent = getListResponse(event.getUser(), CategoryType.EXPENSE);
        event.getBot().sendMessage(event.getUser(), incomeContent + "\n" + expenseContent);
    }

    /**
     * Команда для вывода категорий, доступные пользователю (пользовательские, а также встроенные)
     */
    private void handleListExpenseCategories(HandleCommandEvent event) {
        String content = getListResponse(event.getUser(), CategoryType.EXPENSE);
        event.getBot().sendMessage(event.getUser(), content);
    }

    /**
     * Команда для вывода категорий, доступные пользователю (пользовательские, а также встроенные)
     */
    private void handleListIncomeCategories(HandleCommandEvent event) {
        String content = getListResponse(event.getUser(), CategoryType.INCOME);
        event.getBot().sendMessage(event.getUser(), content);
    }

    /**
     * Получает текст сообщения, которое ожидается для вывода категорий доходов/расходов.
     */
    private String getListResponse(User user, CategoryType type) {
        List<Category> typedStandardCategories = categoryRepository.getStandardCategoriesByType(type);
        List<Category> personalCategories = categoryRepository.getUserCategoriesByType(user, type);

        return StaticMessages.LIST_TYPED_CATEGORIES
                .replace("{type}", type.getPluralShowLabel())
                .replace("{standard_list}", formatCategoryList(typedStandardCategories))
                .replace("{personal_list}", formatCategoryList(personalCategories));
    }

    /**
     * Форматирует список категорий в строку, содержащую нумерованный список из названия этих категорий или
     * {@link StaticMessages#EMPTY_LIST_CONTENT}, если список пуст.
     */
    private String formatCategoryList(List<Category> categories) {
        if (categories.isEmpty()) {
            return StaticMessages.EMPTY_LIST_CONTENT + "\n";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < categories.size(); i++) {
            stringBuilder
                    .append(i + 1)
                    .append(". ")
                    .append(categories.get(i).getCategoryName())
                    .append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Парсит баланс, введенный пользователем.
     * Вернет null если баланс не является числом с плавающей точкой или меньше нуля
     */
    @Nullable
    private Double parseBalanceAmount(String string) throws NumberFormatException {
        double amount = Double.parseDouble(string.replace(",", "."));
        if (amount < 0) throw new NumberFormatException();
        return amount;
    }

    /**
     * Форматирует double в красивую строку.
     * Если число целое, то вернет его без дробной части.
     * Т.е. 1000.0 будет выведено как 1000,
     * а 1000.99 будет выведено как 1000.99
     */
    private String beautifyDouble(double d) {
        if ((int) d == d) return String.valueOf((int) d);
        return String.valueOf(d);
    }

    /**
     * Команда для добавления трат
     * {@link HandleCommandEvent}
     */
    private void handleAddExpense(HandleCommandEvent event) {
        addOperation(event, CategoryType.EXPENSE);
    }

    /**
     * Обработчик для добавления доходов (команда /add_income)
     * {@link HandleCommandEvent}
     */
    private void handleAddIncome(HandleCommandEvent event) {
        addOperation(event, CategoryType.INCOME);
    }

    /**
     * Добавляет Операцию и отправляет пользователю сообщение
     *
     * @param event Event
     * @param type  Тип категории: Расход/доход
     */
    private void addOperation(HandleCommandEvent event, CategoryType type) {
            if (event.getArgs().size() != 2) {
            event.getBot().sendMessage(event.getUser(), StaticMessages.INCORRECT_OPERATION_ARGS_AMOUNT);
            return;
        }
        Operation operation;
        try {
            operation = createOperationRecord(event.getUser(), event.getArgs(), type);
        } catch (CategoryRepository.CategoryNotExistsException e) {
            event.getBot().sendMessage(event.getUser(), StaticMessages.CATEGORY_DOES_NOT_EXISTS);
            return;
        } catch (NumberFormatException e){
            event.getBot().sendMessage(event.getUser(), StaticMessages.INCORRECT_PAYMENT_ARG);
            return;
        }
        double currentBalance = event.getUser().getBalance() + operation.getPayment();
        User user = event.getUser();
        user.setBalance(currentBalance);
        userRepository.saveUser(user);
        String message = type == CategoryType.INCOME
                ? StaticMessages.ADD_INCOME_MESSAGE
                : StaticMessages.ADD_EXPENSE_MESSAGE;
        event.getBot().sendMessage(user,
                message + operation.getCategory().getCategoryName());
    }

    /**
     * Метод для записи в базу операции;
     *
     * @param user Пользователь
     * @param args Аргументы, переданные с командой
     * @param type Расход/Бюджет.
     * @return Совершенная операция
     */
    private Operation createOperationRecord(User user, List<String> args, CategoryType type)
            throws CategoryRepository.CategoryNotExistsException {
        double payment = Double.parseDouble(args.get(0));
        String categoryName = args.get(1);
        if (type == CategoryType.EXPENSE) {
            payment = -Math.abs(payment);
        } else if (type == CategoryType.INCOME) {
            payment = Math.abs(payment);
        }
        Category category = this.categoryRepository.getCategoryByName(user, categoryName, type);
        return this.operationRepository.addOperation(user, category, payment);
    }

    /**
     * Обработчик для команды "/report_expense"
     *
     * @param commandEvent Event
     */
    private void handleReportExpense(HandleCommandEvent commandEvent) {
        if (commandEvent.getArgs().size() != 1) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.INCORRECT_SELF_REPORT_ARGS);
        }
        List<String> parsedArgs = List.of(commandEvent.getArgs().get(0).split("\\."));
        if (!isCorrectReportArgs(parsedArgs.get(0), parsedArgs.get(1))) {
            commandEvent.getBot().sendMessage(commandEvent.getUser(), StaticMessages.INCORRECT_SELF_REPORT_VALUES);
        }
        ReportService service = new ReportService(this.operationRepository);
        Map<String, Double> categoryPaymentMap = service.getExpenseReport(commandEvent.getUser(), parsedArgs);
        StringBuilder message = new StringBuilder();
        message.append(StaticMessages.SELF_REPORT_MESSAGE);
        for (Map.Entry<String, Double> entry : categoryPaymentMap.entrySet()) {
            message.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("руб.\n");
        }
        commandEvent.getBot().sendMessage(commandEvent.getUser(), message.toString());
    }

    private boolean isCorrectReportArgs(String month, String year) {
        int _month = Integer.parseInt(month);
        if (_month < 1 || _month > 12) {
            return false;
        }

        return year.length() == 4 && Integer.parseInt(year) >= 0;
    }
}
