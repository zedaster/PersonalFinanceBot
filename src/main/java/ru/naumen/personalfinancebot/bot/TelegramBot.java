package ru.naumen.personalfinancebot.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.naumen.personalfinancebot.configuration.TelegramBotConfiguration;
import ru.naumen.personalfinancebot.handler.BotHandler;
import ru.naumen.personalfinancebot.handler.event.HandleCommandEvent;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.user.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Телеграм бот
 */
public class TelegramBot extends TelegramLongPollingBot implements Bot {
    private final TelegramBotConfiguration configuration;
    private final BotHandler botHandler;
    private final UserRepository userRepository;

    public TelegramBot(
            TelegramBotConfiguration configuration,
            BotHandler botHandler,
            UserRepository userRepository
    ) {
        super(configuration.getBotToken());
        this.configuration = configuration;
        this.botHandler = botHandler;
        this.userRepository = userRepository;
    }

    /**
     * Обработчик новых событий из библиотеки telegrambots
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
            Long chatId = update.getMessage().getChatId();
            Optional<User> user = this.userRepository.getUserByTelegramChatId(chatId);
            if (user.isEmpty()) {
                user = Optional.of(new User(chatId, 0));
                this.userRepository.saveUser(user.get());
            }
            List<String> msgWords = List.of(update.getMessage().getText().split(" "));
            String cmdName = msgWords.get(0).substring(1);
            List<String> args = msgWords.subList(1, msgWords.size());
            HandleCommandEvent event = new HandleCommandEvent(this, user.get(), cmdName, args);
            this.botHandler.handleCommand(event);
        }
    }

    /**
     * Возвращает bot username
     * Метод необходим для библиотеки telegrambots
     */
    @Override
    public String getBotUsername() {
        return this.configuration.getBotName();
    }

    /**
     * Возвращает bot username
     * Метод необходим для библиотеки telegrambots
     */
    @Override
    public String getBotToken() {
        return this.configuration.getBotToken();
    }

    /**
     * Запуск бота
     */
    @Override
    public void startPooling() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            System.out.println("Telegram bot is pooling now...");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправка текстового сообщения определенному пользователю
     */
    @Override
    public void sendMessage(User user, String text) {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(user.getChatId());
        message.setText(text);

        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
