package ru.naumen.personalfinancebot.models;

/**
 * Enum для обозначения типа категории:
 * INCOME - ДОХОД, EXPENSE - РАСХОД;
 * Важно не менять порядок внутри этого enum, т.к. от него зависит поле {@link Category#getType()}
 */
public enum CategoryType {
    INCOME("income", "доходов"),
    EXPENSE("expense", "расходов");

    /**
     * Именование категории, используемое в командах
     */
    private final String commandLabel;

    /**
     * Название, которое будет показано при выводе множества категории этого типа
     */
    private final String pluralShowLabel;

    CategoryType(String commandLabel, String pluralShowLabel) {
        this.commandLabel = commandLabel;
        this.pluralShowLabel = pluralShowLabel;
    }

    /**
     * Выводит именование категории, используемое в командах.
     * Для CategoryType.INCOME будет выведено "income",
     * а для CategoryType.EXPENSE - "expense".
     */
    public String getCommandLabel() {
        return commandLabel;
    }

    /**
     * Выводит именование категории, используемое для их вывода куда-либо со словом "категории ".
     * Для CategoryType.INCOME будет выведено "доходов",
     * а для CategoryType.EXPENSE - "расходов".
     */
    public String getPluralShowLabel() {
        return pluralShowLabel;
    }
}
