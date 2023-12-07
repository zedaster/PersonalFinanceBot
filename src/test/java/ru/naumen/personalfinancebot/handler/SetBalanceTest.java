package ru.naumen.personalfinancebot.handler;

import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;
import ru.naumen.personalfinancebot.repositories.category.HibernateCategoryRepository;
import ru.naumen.personalfinancebot.repositories.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repositories.operation.OperationRepository;
import ru.naumen.personalfinancebot.repositories.user.HibernateUserRepository;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Тесты для команды /set_balance в FinanceBotHandler
 */
public class SetBalanceTest {
    /**
     * Хранилище пользователей
     */

    private final UserRepository userRepository;

    /**
     * Обработчик всех команд бота
     */
    private final FinanceBotHandler botHandler;

    public SetBalanceTest() {
        HibernateConfiguration hibernateUserRepository = new HibernateConfiguration();
        this.userRepository = new HibernateUserRepository(hibernateUserRepository.getSessionFactory());

        OperationRepository operationRepository = new HibernateOperationRepository(hibernateUserRepository.getSessionFactory());
        CategoryRepository categoryRepository = new HibernateCategoryRepository(hibernateUserRepository.getSessionFactory());
        this.botHandler = new FinanceBotHandler(userRepository, operationRepository, categoryRepository);
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
     * Выполнение команды с правильно введенными значениями
     */
    @Test
    public void correctBalance() {
        List<String> args = List.of(
                "100",
                "0",
                "0.0",
                "0.000000",
                "0,0",
                String.valueOf(Integer.MAX_VALUE)
        );
        List<String> expects = List.of(
                "Ваш баланс изменен. Теперь он составляет 100",
                "Ваш баланс изменен. Теперь он составляет 0",
                "Ваш баланс изменен. Теперь он составляет 0",
                "Ваш баланс изменен. Теперь он составляет 0",
                "Ваш баланс изменен. Теперь он составляет 0",
                "Ваш баланс изменен. Теперь он составляет 2147483647"
        );
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            String expect = expects.get(i);
            assetCorrectBalanceCommand(arg, expect);
        }
    }

    /**
     * Проводит тест с позитивным исходом
     */
    private void assetCorrectBalanceCommand(String argument, String expectedMessage) {
        MockBot mockBot = new MockBot();
        User user = new User(123, 12345);
        userRepository.saveUser(user);
        List<String> args = List.of(argument);
        HandleCommandEvent commandEvent = new HandleCommandEvent(mockBot, user, "set_balance", args);
        this.botHandler.handleCommand(commandEvent);

        Assert.assertEquals(1, mockBot.getMessageQueueSize());
        MockMessage message = mockBot.poolMessageQueue();
        Assert.assertEquals(user, message.receiver());
        double amountDouble = Double.parseDouble(argument.replace(",", "."));
        Assert.assertEquals(expectedMessage, message.text());
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
        Assert.assertEquals(user, message.receiver());
        Assert.assertEquals("Команда введена неверно! Введите /set_balance <новый баланс>", message.text());
        Assert.assertEquals(user.getBalance(), 12345, 1e-15);

        userRepository.removeUserById(user.getId());
    }

}
