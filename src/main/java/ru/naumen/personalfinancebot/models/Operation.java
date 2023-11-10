package ru.naumen.personalfinancebot.models;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Модель данных "операция по расходам/доходам"
 */
@Entity
@Table(name = "operations")
public class Operation {
    /**
     * Идентификатор операции
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    /**
     * Отношение: Пользователь, который произвел операцию
     */
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    /**
     * ID категории расхода/дохода
     */
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    /**
     * Сумма расхода/дохода
     */
    @Column(name = "payment", nullable = false)
    private double payment;

    /**
     * Время записи операции
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Operation(User user, Category category, double payment) {
        this.user = user;
        this.category = category;
        this.payment = payment;
        this.createdAt = LocalDateTime.now();
    }

    public Operation() {

    }

    /**
     * @return ID операции
     */
    public long getId() {
        return id;
    }

    /**
     * @return ID пользователя
     */
    public long getUserId() {
        return user.getId();
    }

    /**
     * @return ID категории расхода/дохода
     */
    public Category getCategory() {
        return category;
    }

    /**
     * @param category ID категории расхода/дохода
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * @return Сумма дохода/расхода
     */
    public double getPayment() {
        return payment;
    }

    /**
     * @param payment Сумма дохода/расхода
     */
    public void setPayment(double payment) {
        this.payment = payment;
    }

    /**
     * @return Время записи операции
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
