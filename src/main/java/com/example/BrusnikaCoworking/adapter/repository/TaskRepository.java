package com.example.BrusnikaCoworking.adapter.repository;

import com.example.BrusnikaCoworking.domain.notification.TaskEntity;
import com.example.BrusnikaCoworking.domain.notification.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findBySendTimeLessThanEqualAndType(LocalDateTime sendTime, Type type);
}