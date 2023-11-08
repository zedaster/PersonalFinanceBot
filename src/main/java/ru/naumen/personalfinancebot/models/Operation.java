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
     * ID пользователя
     */
    @Column(name = "user_id", nullable = false)
    private long userId;

    /**
     * ID категории расхода/дохода
     */
    @Column(name = "category_id", nullable = false)
    private int categoryId;

    /**
     * Сумма расхода/дохода
     */
    @Column(name = "payment", nullable = false)
    private long payment;

    /**
     * Время записи операции
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Отношение: Пользователь, который произвел операцию
     */
    @ManyToOne
    @JoinColumn(name = "users", referencedColumnName = "id")
    private User user;

    public Operation(long id, long userId, int categoryId, long payment, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.payment = payment;
        this.createdAt = createdAt;
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
     * @param id ID операции
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return ID пользователя
     */
    public long getUserId() {
        return userId;
    }

    /**
     * @param userId ID пользователя
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * @return ID категории расхода/дохода
     */
    public int getCategoryId() {
        return categoryId;
    }

    /**
     * @param categoryId ID категории расхода/дохода
     */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * @return Сумма дохода/расхода
     */
    public long getPayment() {
        return payment;
    }

    /**
     * @param payment Сумма дохода/расхода
     */
    public void setPayment(long payment) {
        this.payment = payment;
    }

    /**
     * @return Время записи операции
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt Время записи операции
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
