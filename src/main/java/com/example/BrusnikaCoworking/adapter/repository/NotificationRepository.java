package com.example.BrusnikaCoworking.adapter.repository;

import com.example.BrusnikaCoworking.domain.notification.NotificationEntity;
import com.example.BrusnikaCoworking.domain.notification.TaskEntity;
import com.example.BrusnikaCoworking.domain.reserval.ReservalEntity;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    void deleteByReserval(ReservalEntity reserval);
    void deleteBySendTimeBefore(LocalDateTime sendTime);
    List<NotificationEntity> findByUserOrderBySendTimeDesc(UserEntity user);
}
