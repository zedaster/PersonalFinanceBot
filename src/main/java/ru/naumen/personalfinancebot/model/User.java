package ru.naumen.personalfinancebot.model;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Модель пользователя
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * Идентификатор пользователя
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    /**
     * Идентификатор чата
     */
    @Column(name = "chat_id", unique = true, nullable = false)
    private long chatId;

    /**
     * Баланс пользователя
     */
    @Column(name = "balance")
    private double balance;

    /**
     * Отношение: Операции пользователя
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Operation> operations;

    /**
     * Отношение: Категории, который добавил пользователь
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Category> categories;

    public User(long chatId, double balance) {
        this.chatId = chatId;
        this.balance = balance;
    }

    public User() {}

    /**
     * @return ID пользователя
     */
    public Long getId() {
        return id;
    }

    /**
     * @return ID чата
     */
    public long getChatId(){
        return this.chatId;
    }

    /**
     * Устанавливает баланс пользователя
     * @param balance Баланс пользователя
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * @return Баланс пользователя
     */
    public double getBalance() {
        return this.balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
