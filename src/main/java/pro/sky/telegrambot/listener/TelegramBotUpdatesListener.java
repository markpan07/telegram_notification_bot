package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificatiionTaskRepository;

import javax.annotation.PostConstruct;
import javax.swing.text.DateFormatter;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    @Value("${greetings}")
    private String greetings;
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private TelegramBot telegramBot;
    private NotificatiionTaskRepository notificatiionTaskRepository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificatiionTaskRepository notificatiionTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificatiionTaskRepository = notificatiionTaskRepository;
    }

    private static final Pattern PATTERN = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    private static final DateTimeFormatter DATE_TIME_PATTERN = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: \"{}\", id:{}, from {} {}, username: {}",
                    update.message().text(),
                    update.updateId(),
                    update.message().from().firstName(),
                    update.message().from().lastName(),
                    update.message().from().username());
            var text = update.message().text();
            var chatId = update.message().chat().id();

            if (text.equals("/start")) {
                SendResponse response = telegramBot.execute(new SendMessage(update.message().chat().id(), greetings));
            } else {
                Matcher matcher = PATTERN.matcher(text);
                if (matcher.matches()) {
                    LocalDateTime dateTime = parseDateTime(matcher.group(1));
                    if (dateTime == null) {
                        telegramBot.execute(new SendMessage(chatId, "It is not correspond established DateTime format"));
                        return;
                    }

                    NotificationTask task = new NotificationTask();
                    task.setDateTime(dateTime);
                    task.setText(matcher.group(3));
                    task.setChatId(chatId);

                    NotificationTask saved = notificatiionTaskRepository.save(task);
                    telegramBot.execute(new SendMessage(chatId, "Task is scheduled"));
                    logger.info("Notification task saved {}", saved);
                }
            }
        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private LocalDateTime parseDateTime(String text) {
        try {
            return LocalDateTime.parse(text, DATE_TIME_PATTERN);
        } catch (DateTimeException e) {
            logger.info("{} is not correspond established DateTime format", text);
        }
        return null;
    }

}
