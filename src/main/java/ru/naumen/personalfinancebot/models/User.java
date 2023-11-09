package ru.naumen.personalfinancebot.models;

import javax.persistence.*;
import java.util.List;

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
    private long id;

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
    @OneToMany(mappedBy = "user")
    private List<Operation> operations;

    /**
     * Отношение: Категории, который добавил пользователь
     */
    @OneToMany(mappedBy = "user")
    private List<Category> categories;

    public User(long id, long chatId, double balance) {
        this.id = id;
        this.chatId = chatId;
        this.balance = balance;
    }

    public User() {}

    /**
     * @return ID пользователя
     */
    public long getId() {
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
}
