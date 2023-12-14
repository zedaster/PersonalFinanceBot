package ru.naumen.personalfinancebot.handler.command.budget;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Содержит в себе текущий месяц-год для тестирования бюджета
 */
public class TestYearMonth {
    /**
     * Массив с названия месяцев, ожидаемых к выводу в команде
     */
    private static final String[] MONTH_NAMES = new String[]{
            "Январь",
            "Февраль",
            "Март",
            "Апрель",
            "Май",
            "Июнь",
            "Июль",
            "Август",
            "Сентябрь",
            "Октябрь",
            "Ноябрь",
            "Декабрь",
    };

    /**
     * Месяц-год
     */
    private final YearMonth yearMonth;

    /**
     * Форматировщик месяца-год к виду "01.2024"
     */
    private final DateTimeFormatter dotFormatter;

    TestYearMonth(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
        this.dotFormatter = DateTimeFormatter.ofPattern("MM.yyyy");
    }

    /**
     * Возвращает новый экземпляр TestYearMonth текущего месяца и года
     */
    public static TestYearMonth current() {
        return new TestYearMonth(YearMonth.now());
    }

    /**
     * Возвращает копию с определенным количеством добавленных месяцев
     *
     * @param toAdd количество месяцев, которое нужно добавить
     * @return копию {@link TestYearMonth}
     */
    public TestYearMonth plusMonths(int toAdd) {
        return new TestYearMonth(yearMonth.plusMonths(toAdd));
    }

    /**
     * Возвращает копию с определенным количеством отнятых месяцев
     *
     * @param toSubtract количество месяцев, которое нужно отнять
     * @return копию {@link TestYearMonth}
     */
    public TestYearMonth minusMonths(int toSubtract) {
        return new TestYearMonth(yearMonth.minusMonths(toSubtract));
    }


    /**
     * Возвращает месяц-год в виде ${@link YearMonth}
     */
    public YearMonth getYearMonth() {
        return yearMonth;
    }

    /**
     * Возвращает русскоязычное название месяца. Например, для месяца 1 будет возвращено "Январь".
     */
    public String getMonthName() {
        return MONTH_NAMES[yearMonth.getMonthValue() - 1];
    }

    /**
     * Форматирует этот месяц-год к виду "01.2024"
     *
     * @return строка
     */
    public String getDotFormat() {
        return yearMonth.format(this.dotFormatter);
    }

    /**
     * Возвращает год
     */
    public int getYear() {
        return yearMonth.getYear();
    }

    /**
     * Возвращает дату с этим год, месяцем и указанным днем
     *
     * @param day день
     * @return объект {@link LocalDate}
     */
    public LocalDate atDay(int day) {
        return yearMonth.atDay(day);
    }
}
