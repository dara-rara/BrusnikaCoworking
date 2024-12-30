package com.example.BrusnikaCoworking.service.scheduled;

import com.example.BrusnikaCoworking.adapter.repository.NotificationRepository;
import com.example.BrusnikaCoworking.adapter.repository.ReservalRepository;
import com.example.BrusnikaCoworking.adapter.repository.TaskRepository;
import com.example.BrusnikaCoworking.domain.notification.NotificationEntity;
import com.example.BrusnikaCoworking.domain.reserval.State;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

//    @Scheduled(fixedRate = 60000) // Проверка каждую минуту
//    @Async("taskExecutor")
//    public void checkAndSendNotifications() {
//        var now = LocalDateTime.now();
//        var tasks = taskRepository.findBySendTimeLessThanEqual(now);
//
//        for (var item : tasks) {
//            var reserval = reservalRepository.findById(item.getReserval().getId_reserval()).orElse(null);
//            if (reserval != null && reserval.getStateReserval().equals(State.TRUE)) {
//                var notification = new NotificationEntity();
//                notification.setSendTime(item.getSendTime());
//                notification.setReserval(reserval);
//                notificationRepository.save(notification);
//            }
//            taskRepository.delete(item);
//        }
//    }
    @Async("taskExecutor")
    @Transactional(rollbackFor = Exception.class, timeout = 30) // Тайм-аут 30 секунд
    @Scheduled(fixedRate = 60000) // Запуск каждую минуту
    public void createNotificationsAsync() {
        try {
            var now = LocalDateTime.now();
            var tasks = taskRepository.findBySendTimeLessThanEqual(now); // Получаем задачи

            List<NotificationEntity> notifications = new ArrayList<>();
            List<Long> taskIdsToDelete = new ArrayList<>();

            for (var task : tasks) {
                var reserval = reservalRepository.findById(task.getReserval().getId_reserval()).orElse(null);
                if (reserval != null && reserval.getStateReserval().equals(State.TRUE)) {
                    var notification = new NotificationEntity();
                    notification.setSendTime(task.getSendTime());
                    notification.setReserval(reserval);
                    notification.setUser(reserval.getUser());
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
}
