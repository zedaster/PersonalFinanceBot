package ru.naumen.personalfinancebot.handler;

import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repository.HibernateUserRepository;
import ru.naumen.personalfinancebot.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Тесты для команды /set_balance в FinanceBotHandler
 */
public class SetBalanceTests {
    private final UserRepository userRepository;
    private final BotHandler botHandler;

    public SetBalanceTests() {
        this.userRepository = new HibernateUserRepository(new HibernateConfiguration().getSessionFactory());
        this.botHandler = new FinanceBotHandler(userRepository);
    }

    /**
     * Выполнение команды без аргументов
     */
    @Test
    public void noArguments() {
        assertIncorrectBalanceCommand(new ArrayList<>());
    }

    /**
     * Выполнение команды с 2 аргументами и более
     */
    @Test
    public void tooManyArguments() {
        List<String> twoArgs = List.of("1000", "2000");
        List<String> thereArgs = List.of("1000", "2000", "3000");
        List<String> hundredArgs = IntStream.range(1, 100)
                .mapToObj(Integer::toString)
                .toList();
        List<List<String>> argsCases = List.of(twoArgs, thereArgs, hundredArgs);

        for (List<String> args : argsCases) {
            assertIncorrectBalanceCommand(args);
        }

    }

    /**
     * Выполнение команды с отрицательным значением баланса
     */
    @Test
    public void negativeBalances() {
        List<String> args = List.of("-1", "-1.0", "-1,0", "-0.0001", "-0,0001");
        for (String arg : args) {
            assertIncorrectBalanceCommand(List.of(arg));
        }
    }

    /**
     * Выполнение команды с целочисленными значениями
     */
    @Test
    public void integerBalance() {
        List<String> args = List.of("100", String.valueOf(Integer.MAX_VALUE));
        for (String arg : args) {
            assetCorrectBalanceCommand(arg);
        }
    }

    /**
     * Выполнение команды с числами с плавающей точкой
     */
    @Test
    public void doubleDotBalance() {
        List<String> args = List.of("100.0", String.valueOf(Double.MAX_VALUE));
        for (String arg : args) {
            assetCorrectBalanceCommand(arg);
        }
    }

    /**
     * Выполнение команды с числами с плавающей "запятой"
     */
    @Test
    public void doubleCommaBalance() {
        List<String> args = List.of("100,0", String.valueOf(Double.MAX_VALUE).replace(".", ","));
        for (String arg : args) {
            assetCorrectBalanceCommand(arg);
        }
    }

    /**
     * Выполнение команды с нулевым балансом
     */
    @Test
    public void zeroBalance() {
        List<String> args = List.of("0", "0.0", "00000.0", "0.000000", "0,0");
        for (String arg : args) {
            assetCorrectBalanceCommand(arg);
        }
    }

    /**
     * Проводит тест с позитивным исходом
     */
    private void assetCorrectBalanceCommand(String argument) {
        MockBot mockBot = new MockBot();
        User user = new User(123, 12345);
        userRepository.saveUser(user);
        List<String> args = List.of(argument);
        HandleCommandEvent commandEvent = new HandleCommandEvent(mockBot, user, "set_balance", args);
        this.botHandler.handleCommand(commandEvent);

        Assert.assertEquals(1, mockBot.getMessageQueueSize());
        MockMessage message = mockBot.poolMessageQueue();
        Assert.assertEquals(user, message.sender());
        double amountDouble = Double.parseDouble(argument.replace(",", "."));
        String beautifulAmount = beautifyDouble(amountDouble);
        Assert.assertEquals("Ваш баланс изменен. Теперь он составляет " + beautifulAmount, message.text());
        Assert.assertEquals(user.getBalance(), amountDouble, 1e-15);

        userRepository.removeUserById(user.getId());
    }

    /**
     * Проводит тест с отрицательным исходом
     */
    private void assertIncorrectBalanceCommand(List<String> args) {
        MockBot mockBot = new MockBot();
        User user = new User(123, 12345);
        userRepository.saveUser(user);
        HandleCommandEvent commandEvent = new HandleCommandEvent(mockBot, user, "set_balance", args);
        this.botHandler.handleCommand(commandEvent);

        Assert.assertEquals(1, mockBot.getMessageQueueSize());
        MockMessage message = mockBot.poolMessageQueue();
        Assert.assertEquals(user, message.sender());
        Assert.assertEquals("Команда введена неверно! Введите /set_balance <новый баланс>", message.text());
        Assert.assertEquals(user.getBalance(), 12345, 1e-15);

        userRepository.removeUserById(user.getId());
    }

    /**
     * Форматирует double в красивую строку
     * Если число целое, то вернет его без дробной части.
     * Т.е. 1000.0 будет выведено как 1000
     * А 1000.99 будет выведено как 1000.99
     */
    private String beautifyDouble(double d) {
        if ((int) d == d) return String.valueOf((int) d);
        return String.valueOf(d);
    }

}
