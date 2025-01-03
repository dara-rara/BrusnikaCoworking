package com.example.BrusnikaCoworking.service.scheduled;

import com.example.BrusnikaCoworking.adapter.repository.NotificationRepository;
import com.example.BrusnikaCoworking.adapter.repository.ReservalRepository;
import com.example.BrusnikaCoworking.adapter.repository.TaskRepository;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.KafkaMailMessage;
import com.example.BrusnikaCoworking.config.kafka.KafkaProducer;
import com.example.BrusnikaCoworking.domain.notification.NotificationEntity;
import com.example.BrusnikaCoworking.domain.notification.Type;
import com.example.BrusnikaCoworking.domain.reserval.State;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NotificationScheduler {
    private final TaskRepository taskRepository;
    private final ReservalRepository reservalRepository;
    private final NotificationRepository notificationRepository;
    private final KafkaProducer kafkaProducer;
    private static final String EMAIL_TOPIC_R = "email_message_reserval";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Async("taskExecutor")
    @Transactional(rollbackFor = Exception.class, timeout = 30) // Тайм-аут 30 секунд
    @Scheduled(fixedRate = 60000) // Запуск каждую минуту
    public void createNotificationsAsyncCode() {
        try {
            var now = LocalDateTime.now();
            var tasks = taskRepository.findBySendTimeLessThanEqualAndType(now, Type.CODE); // Получаем задачи

            List<NotificationEntity> notifications = new ArrayList<>();
            List<Long> taskIdsToDelete = new ArrayList<>();

            for (var task : tasks) {
                var reserval = reservalRepository.findById(task.getReserval().getId_reserval()).orElse(null);
                if (reserval != null && reserval.getStateReserval().equals(State.TRUE)) {
                    var notification = new NotificationEntity();
                    notification.setSendTime(task.getSendTime());
                    notification.setReserval(reserval);
                    notification.setUser(reserval.getUser());
                    notification.setType(Type.CODE);
                    notifications.add(notification);
                }
                taskIdsToDelete.add(task.getId_task()); // Добавляем задачу для удаления
            }

            // Пакетное сохранение уведомлений
            notificationRepository.saveAll(notifications);

            // Пакетное удаление задач
            taskRepository.deleteAllById(taskIdsToDelete);
        } catch (Exception e) {
            // Логирование исключения
            System.err.println(e.getMessage());
            throw e; // Повторное выбрасывание исключения для отката транзакции
        }
    }

    @Async("taskExecutor")
    @Transactional(rollbackFor = Exception.class, timeout = 30) // Тайм-аут 30 секунд
    @Scheduled(cron = "0 0 */6 * * *") // Запуск каждые 6 часов
    public void createNotificationsAsyncMemento() {
        try {
            var now = LocalDateTime.now();
            var tasks = taskRepository.findBySendTimeLessThanEqualAndType(now, Type.MEMENTO); // Получаем задачи с типом NOTIFICATION

            List<Long> taskIdsToDelete = new ArrayList<>();
            var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            for (var task : tasks) {
                var reserval = reservalRepository.findById(task.getReserval().getId_reserval()).orElse(null);
                if (reserval != null && reserval.getStateReserval().equals(State.TRUE)) {
                    kafkaProducer.produce(EMAIL_TOPIC_R, new KafkaMailMessage(reserval.getUser().getUsername(),
                            reserval.getDate().format(formatter)));
                }
                taskIdsToDelete.add(task.getId_task()); // Добавляем задачу для удаления
            }
            // Пакетное удаление задач
            taskRepository.deleteAllById(taskIdsToDelete);
        } catch (Exception e) {
            // Логирование исключения
            System.err.println(e.getMessage());
            throw e; // Повторное выбрасывание исключения для отката транзакции
        }
    }
}
