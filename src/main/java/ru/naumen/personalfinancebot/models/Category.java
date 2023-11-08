package ru.naumen.personalfinancebot.models;

import javax.persistence.*;

/**
 * Модель данных "Категория расхода/дохода"
 */
@Entity
@Table(name = "categories")
public class Category {
    /**
     * ID категории
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    /**
     * Отношение: Пользователь, который добавил каатегорию.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    /**
     * Название категории
     */
    @Column(name = "category_name", nullable = false)
    private String categoryName;

    /**
     * Тип категории: Расход / Доход
     */
    @Column(name = "type", nullable = false)
    private String type;

    public Category(int id, User user, String categoryName, String type) {
        this.id = id;
        this.user = user;
        this.categoryName = categoryName;
        this.type = type;
    }

    public Category() {

    }

    /**
     * @return ID категории
     */
    public int getId() {
        return id;
    }

    /**
     * @return ID пользователя
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user ID пользователя
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return Название категории
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * @param categoryName Название категории
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * @return Тип категории
     */
    public String getType() {
        return type;
    }

    /**
     * @param type Тип категории
     */
    public void setType(String type) {
        this.type = type;
    }
}
