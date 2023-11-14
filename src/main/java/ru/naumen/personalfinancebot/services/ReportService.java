package ru.naumen.personalfinancebot.services;

import ru.naumen.personalfinancebot.models.Operation;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для подготовки отчётов по расходам
 */
public class ReportService {
    private final OperationRepository operationRepository;

    public ReportService(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    /**
     * Возвращет hashmap с категорией расходов и суммой затрат по категории
     *
     * @param user Пользователь, для которого надо вернуть отчёт
     * @param args Аргументы, переданные вместе с командой
     * @return "Словарь" с категориями и затратоми
     */
    public Map<String, Double> getExpenseReport(User user, List<String> args) {
        int month = Integer.parseInt(args.get(0));
        int year = Integer.parseInt(args.get(1));
        List<Operation> operations = this.operationRepository.getFilteredByDate(user, month, year);
        Map<String, Double> map = new HashMap<>();
        for (Operation operation : operations) {
            map.put(
                    operation.getCategory().getCategoryName(),
                    // Округление до 2-ух знаков
                    Math.round(operation.getPayment() * 100.0) / 100.0
            );
        }
        return map;
    }
}
