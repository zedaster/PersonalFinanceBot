package ru.naumen.personalfinancebot.repository.fake;

import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;

import java.time.LocalDate;

/**
 * Фейковая операция для тестов
 * Позволяет поставить ту дату, которую нужно
 */
public class FakeOperation extends Operation {
    private final LocalDate createdAt;

    public FakeOperation(User user, Category category, double payment, LocalDate createdAt) {
        super(user, category, payment);
        this.createdAt = createdAt;
    }

    public FakeOperation(User user, Category category, double payment) {
        super(user, category, payment);
        this.createdAt = super.getCreatedAt();
    }

    /**
     * @return Время записи операции
     */
    public LocalDate getCreatedAt() {
        return createdAt;
    }
}
