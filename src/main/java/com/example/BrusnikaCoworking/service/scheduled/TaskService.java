package com.example.BrusnikaCoworking.service.scheduled;

import com.example.BrusnikaCoworking.adapter.repository.TaskRepository;
import com.example.BrusnikaCoworking.domain.notification.TaskEntity;
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

    public void scheduleNotification(ReservalEntity reserval, LocalDateTime reservalTime) {
        var sendTime = reservalTime.minusMinutes(30); // Уведомление за 30 минут до бронирования
        var task = new TaskEntity();
        task.setReserval(reserval);
        task.setSendTime(sendTime);
        taskRepository.save(task);
    }

}
