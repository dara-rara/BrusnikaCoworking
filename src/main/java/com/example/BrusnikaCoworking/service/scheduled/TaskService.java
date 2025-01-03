package com.example.BrusnikaCoworking.service.scheduled;

import com.example.BrusnikaCoworking.adapter.repository.TaskRepository;
import com.example.BrusnikaCoworking.domain.notification.TaskEntity;
import com.example.BrusnikaCoworking.domain.notification.Type;
import com.example.BrusnikaCoworking.domain.reserval.ReservalEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TaskService {
    private final TaskRepository taskRepository;

    public void scheduleNotificationCode(ReservalEntity reserval, LocalDateTime reservalTime) {
        var sendTime = reservalTime.minusMinutes(30); // Уведомление за 30 минут до бронирования
        var task = new TaskEntity();
        task.setReserval(reserval);
        task.setSendTime(sendTime);
        task.setType(Type.CODE);
        taskRepository.save(task);
    }

    public void scheduleNotificationMemento(ReservalEntity reserval, LocalDateTime reservalTime) {
        var sendTime = reservalTime.minusMinutes(60 * 24); // Уведомление за день до бронирования
        var task = new TaskEntity();
        task.setReserval(reserval);
        task.setSendTime(sendTime);
        task.setType(Type.MEMENTO);
        taskRepository.save(task);
    }
}
