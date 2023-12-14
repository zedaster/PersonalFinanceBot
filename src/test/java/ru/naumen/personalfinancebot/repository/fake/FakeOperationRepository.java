package ru.naumen.personalfinancebot.repository.fake;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Хранилище операций для тестов
 */
public class FakeOperationRepository implements OperationRepository {
    /**
     * Коллекция, в которой хранятся все операции
     */
    private final Map<UserCategoryType, List<FakeOperation>> operations;

    public FakeOperationRepository() {
        this.operations = new HashMap<>();
    }

    /**
     * Метод для добавления операции
     *
     * @param user     Пользователь, совершивший операцию
     * @param category Категория дохода/расхода
     * @param payment  Сумма
     * @return совершённая операция
     */
    @Override
    public Operation addOperation(Session session, User user, Category category, double payment) {
        FakeOperation operation = new FakeOperation(user, category, payment);
        return saveFakeOperation(user, category, operation);
    }

    /**
     * Метод для добавления операции с указанной датой
     *
     * @param user      Пользователь, совершивший операцию
     * @param category  Категория дохода/расхода
     * @param payment   Сумма
     * @param createdAt Дата создания операции
     * @return совершённая операция
     */
    public FakeOperation addOperation(Session session, User user, Category category, double payment, LocalDate createdAt) {
        FakeOperation operation = new FakeOperation(user, category, payment, createdAt);
        return saveFakeOperation(user, category, operation);
    }

    /**
     * Внутренний метод для сохранения операции в коллекцию
     */
    private FakeOperation saveFakeOperation(User user, Category category, FakeOperation operation) {
        UserCategoryType userCategoryType = new UserCategoryType(user, category.getType());
        List<FakeOperation> existingOperations = operations.computeIfAbsent(userCategoryType, k -> new ArrayList<>());
        existingOperations.add(operation);
        return operation;
    }

    /**
     * Возвращает словарь с названием категории и суммой расходов/доходов этой категории за указанный год и месяц
     *
     * @param user  Пользователь
     * @param month Месяц
     * @param year  Год
     * @return Список операций или null, если запрашиваемые операции отсутствуют
     */
    @Override
    public Map<String, Double> getOperationsSumByType(Session session, User user, int month, int year, CategoryType type) {
        UserCategoryType userCategoryType = new UserCategoryType(user, type);
        Map<String, List<Operation>> groupedOperations = operations.get(userCategoryType)
                .stream()
                .filter(o -> o.getCreatedAt().getYear() == year && o.getCreatedAt().getMonth().getValue() == month)
                .collect(Collectors.groupingBy(o -> o.getCategory().getCategoryName()));

        if (groupedOperations.isEmpty()) {
            return null;
        }

        Map<String, Double> resultMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<Operation>> entry : groupedOperations.entrySet()) {
            Double sum = entry.getValue()
                    .stream()
                    .mapToDouble(Operation::getPayment)
                    .sum();
            resultMap.put(entry.getKey(), sum);
        }

        return resultMap;
    }

    /**
     * Метод возвращает сумму операций пользователя указанного типа (расход/доход) за определённый месяц
     *
     * @param user      Пользователь
     * @param type      Тип операции
     * @param yearMonth Месяц, год
     * @return Сумма операций
     */
    @Override
    public double getCurrentUserPaymentSummary(Session session, User user, CategoryType type, YearMonth yearMonth) {
        UserCategoryType userCategoryType = new UserCategoryType(user, type);
        List<FakeOperation> fakeOperations = operations.get(userCategoryType);
        if (fakeOperations == null) {
            return 0;
        }
        return fakeOperations
                .stream()
                .filter(o -> o.getCreatedAt().getYear() == yearMonth.getYear()
                        && o.getCreatedAt().getMonth().equals(yearMonth.getMonth()))
                .mapToDouble(Operation::getPayment)
                .sum();
    }


    @Override
    public Map<CategoryType, Double> getEstimateSummary(Session session, YearMonth yearMonth) {
        return null;
    }

    /**
     * Удалает все сохраненные операции
     */
    public void removeAll() {
        this.operations.clear();
    }

    /**
     * Запись, содержащая в себе пользователя и тип категории. Необходима для группировки операций
     *
     * @param user         Пользователь
     * @param categoryType Тип категории
     */
    private record UserCategoryType(User user, CategoryType categoryType) {

    }
}
