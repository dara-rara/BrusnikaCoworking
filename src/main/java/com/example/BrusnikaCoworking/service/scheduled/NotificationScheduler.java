package com.example.BrusnikaCoworking.service.scheduled;

import com.example.BrusnikaCoworking.adapter.repository.*;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.KafkaMailMessage;
import com.example.BrusnikaCoworking.config.kafka.KafkaProducer;
import com.example.BrusnikaCoworking.domain.notification.NotificationEntity;
import com.example.BrusnikaCoworking.domain.notification.Type;
import com.example.BrusnikaCoworking.domain.reserval.CodeEntity;
import com.example.BrusnikaCoworking.domain.reserval.ReservalEntity;
import com.example.BrusnikaCoworking.domain.reserval.State;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
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
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NotificationScheduler {
    private final TaskRepository taskRepository;
    private final ReservalRepository reservalRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CodeRepository codeRepository;
    private final KafkaProducer kafkaProducer;
    private static final String EMAIL_TOPIC_R = "email_message_reserval";


    @Async("taskExecutor")
    @Transactional(rollbackFor = Exception.class, timeout = 30) // Тайм-аут 30 секунд
    @Scheduled(fixedRate = 60000 * 5) // Запуск каждые 5 минут
    public void createNotificationsCode() {
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

            // Пакетное сохранение
            notificationRepository.saveAll(notifications);

            // Пакетное удаление
            taskRepository.deleteAllById(taskIdsToDelete);
        } catch (Exception e) {
            // Логирование исключения
            System.err.println(e.getMessage());
            throw e; // Повторное выбрасывание исключения для отката транзакции
        }
    }

    @Async("taskExecutor")
    @Transactional(rollbackFor = Exception.class, timeout = 30) // Тайм-аут 30 секунд
    @Scheduled(cron = "0 0 */5 * * *") // Запуск каждые 5 часов
    public void createNotificationsMemento() {
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
            // Пакетное удаление
            taskRepository.deleteAllById(taskIdsToDelete);
        } catch (Exception e) {
            // Логирование исключения
            System.err.println(e.getMessage());
            throw e; // Повторное выбрасывание исключения для отката транзакции
        }
    }

    @Async("taskExecutor")
    @Transactional(rollbackFor = Exception.class, timeout = 30) // Тайм-аут 30 секунд
    @Scheduled(cron = "0 0 */3 * * *") // Запуск каждые 3 часов
    public void checkReservationsCreated24HoursAgoWithStateGroupTrue() {
        try {
            var twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
            var reservals =
                    reservalRepository.findBySendTimeBeforeAndStateGroup(twentyFourHoursAgo, State.TRUE);

            for (var item : reservals) {
                // Логика обработки бронирований
                item.setStateReserval(State.FALSE);
                item.setStateGroup(State.UNCONFIRMED);
                reservalRepository.save(item);
            }

        } catch (Exception e) {
            // Логирование исключения
            System.err.println(e.getMessage());
            throw e; // Повторное выбрасывание исключения для отката транзакции
        }
    }

    @Async("taskExecutor")
    @Transactional(rollbackFor = Exception.class, timeout = 60) // Тайм-аут 60 секунд
    @Scheduled(cron = "0 0 3 * * *") // Запуск каждый день в 03:00
    public void checkUnconfirmedReservals() {
        try {
            var now = LocalDateTime.now();
            var unconfirmedReservals =
                    reservalRepository.findByStateReservalAndDateBeforeAndStateGroup(now.toLocalDate());
            List<ReservalEntity> reservals = new ArrayList<>();
            for (var reserval : unconfirmedReservals) {
                var user = reserval.getUser();
                user.setCountBlock(user.getCountBlock() + 1);
                userRepository.save(user);
                reserval.setStateReserval(State.UNCONFIRMED);
                reservals.add(reserval);
            }
            // Пакетное сохранение
            reservalRepository.saveAll(reservals);

            //Проверка ложных приглашений других людей
            var unconfirmedGroup =  reservalRepository.findByStateGroup(State.UNCONFIRMED);
            for (var reserval : unconfirmedGroup) {
                var user = reserval.getInvit();
                user.setCountBlock(user.getCountBlock() + 1);
                userRepository.save(user);
                reserval.setStateGroup(State.VERIFIED);
                reservals.add(reserval);
            }
            // Пакетное сохранение
            reservalRepository.saveAll(reservals);

        } catch (Exception e) {
            // Логирование исключения
            System.err.println(e.getMessage());
            throw e; // Повторное выбрасывание исключения для отката транзакции
        }
    }

    @Async("taskExecutor")
    @Transactional(rollbackFor = Exception.class, timeout = 60) // Тайм-аут 60 секунд
    @Scheduled(cron = "0 0 2 * * *") // Запуск каждый день в 02:00
    public void getRandomCode() {
        try {
            var now = LocalDateTime.now();

            var random = new Random();
            var sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                // Генерация случайного числа в диапазоне
                var randomAscii = 33 + random.nextInt(126 - 33 + 1);
                // Преобразуем число в символ и добавляем в код
                sb.append((char) randomAscii);
            }

            var code = new CodeEntity();
            code.setSendTime(now);
            code.setCode(sb.toString());
            codeRepository.save(code);

        } catch (Exception e) {
            // Логирование исключения
            System.err.println(e.getMessage());
            throw e; // Повторное выбрасывание исключения для отката транзакции
        }
    }

    @Async("taskExecutor")
    @Transactional(rollbackFor = Exception.class, timeout = 100) // Тайм-аут 100 секунд
    @Scheduled(cron = "0 0 1 ? */5 SAT") // Запуск в 05:00 каждый месяц по субботам
    public void deleteOldReservalsAndNotifications() {
        try {
            var MonthAgo = LocalDateTime.now().minusMonths(1); // Дата 5 месяцев назад
            List<ReservalEntity> oldReservals = reservalRepository.findByDateBefore(MonthAgo.toLocalDate());
            //при реальном использовании лучше делать через каскадное удалени,
            //но там есть вопросы с конкуретным доступом к ReservalEntity, так как при создании уведомления,
            //нужно менять сущность ReservalEntity(сохранять NotificationEntity в список ReservalEntity)
            for (var reserval : oldReservals) {
                // Удаляем связанные уведомления
                notificationRepository.deleteByReserval(reserval);
            }
            reservalRepository.deleteAll(oldReservals);
            notificationRepository.deleteBySendTimeBefore(MonthAgo);
            codeRepository.deleteBySendTimeBefore(MonthAgo);
        } catch (Exception e) {
            // Логирование исключения
            System.err.println(e.getMessage());
            throw e; // Повторное выбрасывание исключения для отката транзакции
        }
    }
}
