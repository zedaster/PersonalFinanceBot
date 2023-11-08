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
     * ID пользователя
     * <p>Если != null, то относиться к конкретному пользователю, который её добавил</p>
     */
    @Column(name = "user_id")
    private long userId;

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

    /**
     * Отношение: Пользователь, который добавил каатегорию.
     */
    @ManyToOne
    @JoinColumn(name = "users", referencedColumnName = "id")
    private User user;

    public Category(int id, long userId, String categoryName, String type) {
        this.id = id;
        this.userId = userId;
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
     * @param id ID категории
     */
    public void setId(int id) {
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
