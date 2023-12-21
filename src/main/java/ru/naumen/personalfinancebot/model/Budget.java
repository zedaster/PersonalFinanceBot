package ru.naumen.personalfinancebot.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Модель данных "Бюджет"
 */
@Entity
@Table(name = "budgets")
public class Budget {
    /**
     * День месяца, который указывается в типе данных {@link LocalDate}
     */
    private static final int FIRST_DAY_OF_MONTH = 1;

    /**
     * Уникальный идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private long id;

    /**
     * Внешний ключ на пользователя
     */
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    /**
     * Планируемый доход
     */
    @Column(name = "income", nullable = false)
    private double income;

    /**
     * Планируемый расход
     */
    @Column(name = "expense", nullable = false)
    private double expense;

    /**
     * Дата, обозначающая год и месяц, за которыми закреплён ожидаемый бюджет
     * <p>Java-тип {@link YearMonth}, СУБД тип {@link LocalDate} (Date)</p>
     */
    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    public Budget() {

    }

    /**
     * @param user      Пользователь
     * @param income    Ожидаемый доход
     * @param expense   Ожидаемый расход
     * @param yearMonth Год-месяй
     */
    public Budget(User user, double income, double expense, YearMonth yearMonth) {
        this.user = user;
        this.income = income;
        this.expense = expense;
        this.targetDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), FIRST_DAY_OF_MONTH);
    }

    /**
     * @return Уникальный идентификатор
     */
    private long getId() {
        return this.id;
    }


    /**
     * @return Год-Месяц бюджета
     */
    public YearMonth getTargetDate() {
        return YearMonth.of(this.targetDate.getYear(), this.targetDate.getMonth());
    }

    /**
     * Устанавливает месяц-год бюджета
     *
     * @param targetDate Год-Месяц
     */
    public void setTargetDate(YearMonth targetDate) {
        this.targetDate = LocalDate.of(targetDate.getYear(), targetDate.getMonth(), FIRST_DAY_OF_MONTH);
    }

    /**
     * Устанавливает ожидаемый расход
     * @param expense Сумма расходов
     */
    public void setExpense(double expense) {
        this.expense = expense;
    }

    /**
     * @return Ожидаемый расход
     */
    public double getExpense() {
        return this.expense;
    }

    /**
     * Устанавливает ожидаемый доход
     * @param income Сумма доходов
     */
    public void setIncome(double income) {
        this.income = income;
    }

    /**
     * @return Ожидаемый расход
     */
    public double getIncome() {
        return this.income;
    }

    /**
     * @return Пользователь - создатель бюджета
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Устанавливает пользователя, планирующего бюджет на месяц
     *
     * @param user Пользователь
     */
    public void setUser(User user) {
        this.user = user;
    }
}
