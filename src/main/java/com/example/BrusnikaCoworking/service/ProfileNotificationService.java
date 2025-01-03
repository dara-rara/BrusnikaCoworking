package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.repository.NotificationRepository;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.MessageResponse;
import com.example.BrusnikaCoworking.adapter.web.user.dto.notification.NotificationAndReserval;
import com.example.BrusnikaCoworking.adapter.web.user.dto.profile.Profile;
import com.example.BrusnikaCoworking.adapter.web.user.dto.notification.NotificationForm;
import com.example.BrusnikaCoworking.domain.notification.Type;
import com.example.BrusnikaCoworking.domain.reserval.State;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProfileNotificationService {
    private final NotificationRepository notificationRepository;
    private final ReservalService reservalService;

    public Profile getProfile(UserEntity user) {
         return new Profile(user.getUsername(), user.getRealname());
    }

    public MessageResponse confirmGroupReserval(Long id) {
        var notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        return reservalService.updateStateGroup(notification.getReserval());
    }

    public NotificationAndReserval getListsNotificationAndReserval (UserEntity user) {
        return new NotificationAndReserval(reservalService.reservalsActiveUser(user),
                allNotification(user));
    }

    public List<NotificationForm> allNotification (UserEntity user) {
        List<NotificationForm> notifications = new ArrayList<>();
        var notificationsEntity = notificationRepository.findByUserOrderBySendTimeDesc(user);
        for(var item : notificationsEntity) {
            var reserval = item.getReserval();
            var invit = "";
            var state = false;
            if (item.getType().equals(Type.CODE)) {
                //чтобы была возможность подтвердить бронь кодом
                if (reserval.getStateReserval().equals(State.TRUE)) state = true;
            }
            else {
                invit = reserval.getInvit().getUsername();
                //чтобы была возможность подтвердить бронь при приглашении
                if (reserval.getStateGroup().equals(State.TRUE)) state = true;
            }

            var form = new NotificationForm(
                    item.getId_notif(),
                    DateTimeFormatter.ofPattern("dd.MM.YYYY").format(reserval.getDate()),
                    DateTimeFormatter.ofPattern("HH:mm").format(reserval.getTimeStart()),
                    DateTimeFormatter.ofPattern("HH:mm").format(reserval.getTimeEnd()),
                    reserval.getTable().getNumber(),
                    DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm").format(item.getSendTime()),
                    item.getType(),
                    state,
                    invit
            );
            notifications.add(form);
        }
        return notifications;
    }
}
