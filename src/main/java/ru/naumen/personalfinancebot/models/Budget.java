package ru.naumen.personalfinancebot.models;

import java.time.YearMonth;

/**
 * Модель данных "Бюджет"
 */
public class Budget {

    /**
     * @return Месяц-Год бюджета
     */
    public YearMonth getYearMonth() {
        return null;
    }

    /**
     * Устанавливает месяц-год бюджета
     *
     * @param yearMonth месяц-год
     */
    public void setYearMonth(YearMonth yearMonth) {
        // TODO
    }

    /**
     * Устанавливает ожидаемую сумма дохода/расхода
     *
     * @param type   Расход/Доход
     * @param income Сумма
     */
    public void setExpectedSummary(CategoryType type, double income) {
        // TODO
    }

    /**
     * Возвращает ожидаемую сумму дохода/расхода
     *
     * @param type Расход/Доход
     * @return Сумма расхода/дохода
     */
    public double getExpectedSummary(CategoryType type) {
        // TODO
        return 0.0;
    }

    /**
     * Устанавливает пользователя, планирующего бюджет на месяц
     *
     * @param user Пользователь
     */
    public void setUser(User user) {
        // TODO
    }
}
