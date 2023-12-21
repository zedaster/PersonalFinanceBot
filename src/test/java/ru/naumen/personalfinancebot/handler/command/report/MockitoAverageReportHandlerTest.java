package ru.naumen.personalfinancebot.handler.command.report;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import ru.naumen.personalfinancebot.bot.MockBot;
import ru.naumen.personalfinancebot.bot.MockMessage;
import ru.naumen.personalfinancebot.handler.command.CommandHandler;
import ru.naumen.personalfinancebot.handler.commandData.CommandData;
import ru.naumen.personalfinancebot.model.Operation;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.operation.HibernateOperationRepository;
import ru.naumen.personalfinancebot.repository.operation.OperationRepository;
import ru.naumen.personalfinancebot.service.DateParseService;
import ru.naumen.personalfinancebot.service.OutputMonthFormatService;
import ru.naumen.personalfinancebot.service.OutputNumberFormatService;
import ru.naumen.personalfinancebot.service.ReportService;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для тестирования обработчика {@link AverageReportHandler}
 * Тесты написаны с использованием Mockito
 */
public class MockitoAverageReportHandlerTest {
    /**
     * Команда, вводимая пользователем
     */
    private final String COMMAND_NAME = "avg_report";

    /**
     * Репозиторий для работы с {@link Operation}
     */
    private final OperationRepository operationRepository;

    /**
     * Обработчик
     */
    private final CommandHandler handler;

    /**
     * Пользователь
     */
    private final User user = new User(1L, 100_000);

    /**
     * Моковый бот
     */
    private MockBot bot;

    public MockitoAverageReportHandlerTest() {
        DateParseService dateParseService = new DateParseService();
        this.operationRepository = Mockito.mock(HibernateOperationRepository.class);
        OutputMonthFormatService monthFormatService = new OutputMonthFormatService();
        OutputNumberFormatService numberFormatService = new OutputNumberFormatService();
        ReportService reportService = new ReportService(operationRepository, monthFormatService, numberFormatService);
        this.handler = new AverageReportHandler(dateParseService, reportService);
    }

    @Before
    public void initMockBot() {
        this.bot = new MockBot();
    }

    /**
     * Проверяет, что обработчик вернет сообщение с об отсутствии данных,
     * при условии что операций не существует и <b>пользователь передал дату</b>.
     */
    @Test
    public void handleCommandIfNoDataSpecificDate() {
        YearMonth yearMonth = YearMonth.of(2023, 12);
        String expected = "На заданный промежуток данные отсутствуют.";
        Mockito.when(this.operationRepository.getAverageSummaryByStandardCategory(null, yearMonth))
                .thenReturn(null);
        CommandData commandData = new CommandData(this.bot, this.user, COMMAND_NAME, List.of("12.2023"));
        this.handler.handleCommand(commandData, null);

        MockMessage message = this.bot.poolMessageQueue();
        Assert.assertEquals(expected, message.text());
    }

    /**
     * Проверяет, что обработчик вернет сообщение с об отсутствии данных,
     * при условии что операций не существует и <b>пользователь не передал дату</b>.
     */
    @Test
    public void handleCommandIfNoDataCurrentDate() {
        YearMonth yearMonth = YearMonth.now();
        String expected = "На этот месяц данные отсутствуют.";
        Mockito.when(this.operationRepository.getAverageSummaryByStandardCategory(null, yearMonth))
                .thenReturn(null);
        CommandData commandData = new CommandData(this.bot, this.user, COMMAND_NAME, List.of());
        this.handler.handleCommand(commandData, null);

        MockMessage message = this.bot.poolMessageQueue();
        Assert.assertEquals(expected, message.text());
    }

    /**
     * Метод проверяет, что отчет выведется, если есть данные по операция хотя бы по одной стандартной категории
     */
    @Test
    public void handleCommandIfSomeDataExists() {
        YearMonth yearMonth = YearMonth.of(2023, 12);
        Map<String, Double> map = new LinkedHashMap<>() {{
            put("Супермаркеты", 1.0);
            put("Зарплата", 0.0);
            put("Рестораны и кафе", 0.0);
        }};
        String expected = """
                Подготовил отчет по стандартным категориям со всех пользователей за Декабрь 2023:
                Супермаркеты: 1 руб.
                Зарплата: 0 руб.
                Рестораны и кафе: 0 руб.
                """;
        Mockito.when(this.operationRepository.getAverageSummaryByStandardCategory(null, yearMonth))
                .thenReturn(map);
        CommandData commandData = new CommandData(this.bot, this.user, COMMAND_NAME, List.of());
        this.handler.handleCommand(commandData, null);

        MockMessage message = this.bot.poolMessageQueue();
        Assert.assertEquals(expected, message.text());
    }

    /**
     * Тест на то, что числа будет форматированы при условии что данные существуют
     */
    @Test
    public void handleCommandCheckFormattedData() {
        YearMonth yearMonth = YearMonth.of(2023, 12);
        Map<String, Double> map = new LinkedHashMap<>() {{
            put("Супермаркеты", 1234.1);
            put("Зарплата", 100_000.99);
            put("Рестораны и кафе", 99_999.90);
        }};

        String expected = """
                Подготовил отчет по стандартным категориям со всех пользователей за Декабрь 2023:
                Супермаркеты: 1 234,1 руб.
                Зарплата: 100 001 руб.
                Рестораны и кафе: 99 999,9 руб.
                """;

        Mockito.when(this.operationRepository.getAverageSummaryByStandardCategory(null, yearMonth))
                .thenReturn(map);
        CommandData data = new CommandData(this.bot, this.user, COMMAND_NAME, List.of("12.2023"));
        this.handler.handleCommand(data, null);

        MockMessage message = this.bot.poolMessageQueue();
        Assert.assertEquals(expected, message.text());
    }

    /**
     * Тестирует, что при неверно переданной дате обработчик выведет сообщение об ошибке
     */
    @Test
    public void handleCommandWithIncorrectDateFormat() {
        CommandData data = new CommandData(this.bot, this.user, COMMAND_NAME, List.of("pp.pppp"));
        this.handler.handleCommand(data, null);

        MockMessage message = this.bot.poolMessageQueue();
        Assert.assertEquals(
                "Дата введена неверно! Введите ее в формате [mm.yyyy - месяц.год]", message.text()
        );
    }

    /**
     * Тестирует на неверно переданное кол-во аргументов
     */
    @Test
    public void handleCommandWithIncorrectArgsCount() {
        String expected = """
                Команда "/avg_report" не принимает аргументы, либо принимает Месяц и Год в формате "MM.YYYY"
                Например, "/avg_report" или "/avg_report 12.2023".""";

        List<List<String>> argsList = List.of(
                List.of("12.2023", "12.2023"),
                List.of("12", "2023"),
                List.of("p", "pppp", "12.2023"),
                List.of("", "", "")
        );
        for (List<String> args : argsList) {
            CommandData data = new CommandData(this.bot, this.user, COMMAND_NAME, args);
            this.handler.handleCommand(data, null);

            MockMessage message = this.bot.poolMessageQueue();
            Assert.assertEquals(expected, message.text());
        }
    }
}