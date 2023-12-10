package pro.sky.telegrambot.notifier;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.repository.NotificatiionTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class TaskNotifier {

    private final static Logger logger = LoggerFactory.getLogger(TaskNotifier.class);
    private TelegramBot telegramBot;
    private NotificatiionTaskRepository notificatiionTaskRepository;

    public TaskNotifier(TelegramBot telegramBot, NotificatiionTaskRepository notificatiionTaskRepository) {
        this.telegramBot = telegramBot;
        this.notificatiionTaskRepository = notificatiionTaskRepository;
    }

    @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 1)
    public void notifyTask() {
        notificatiionTaskRepository.findAllByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                .forEach(notificationTask -> {
                    telegramBot.execute(new SendMessage(notificationTask.getChatId(), notificationTask.getText()));
                    logger.info("{} has been sent", notificationTask);
                    notificationTask.setHasBeenSent(true);
                    notificatiionTaskRepository.save(notificationTask);
                    logger.info("The \"sent flag\" has been set in {}", notificationTask);

                });
    }
}
