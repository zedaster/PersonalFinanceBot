package ru.naumen.personalfinancebot.handler.command.budget;

import org.junit.Assert;
import org.junit.Test;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.configuration.HibernateConfiguration;
import ru.naumen.personalfinancebot.handler.FinanceBotHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.empty.EmptyBudgetRepository;
import ru.naumen.personalfinancebot.repository.empty.EmptyCategoryRepository;
import ru.naumen.personalfinancebot.repository.empty.EmptyOperationRepository;
import ru.naumen.personalfinancebot.repository.empty.EmptyUserRepository;

import java.util.List;

/**
 * Тесты для команды "/budget_help"
 */
public class HelpBudgetTest {
    /**
     * Тест на вывод сообщения командой "/budget_help"
     */
    @Test
    public void testBudgetHelpCommand() {
        MockBot mockBot = new MockBot();
        FinanceBotHandler handler = new FinanceBotHandler(
                new EmptyUserRepository(),
                new EmptyOperationRepository(),
                new EmptyCategoryRepository(),
                new EmptyBudgetRepository());
        User user = new User(1L, 100);
        CommandData command = new CommandData(mockBot, user, "budget_help", List.of());
        handler.handleCommand(command, null);

        Assert.assertEquals(1, mockBot.getMessageQueueSize());
        MockMessage lastMessage = mockBot.poolMessageQueue();
        Assert.assertEquals("""
                        Доступные команды для работы с бюджетами:
                        /budget - показать бюджет за текущий месяц
                        /budget_list - запланированный бюджет за последние 12 месяцев
                        /budget_list [yyyy - год] -  запланированные бюджеты за определенный год
                        /budget_list [mm.yyyy from - месяц от] [mm.year to - месяц до] - запланированные бюджеты за определенные месяца
                        /budget_create [mm.yyyy - месяц.год] [ожидаемый доход] [ожидаемый расходы] - планировать бюджет""",
                lastMessage.text());
        Assert.assertEquals(user, lastMessage.receiver());
    }
}
