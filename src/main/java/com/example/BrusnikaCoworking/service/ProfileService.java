package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.repository.CoworkingRepository;
import com.example.BrusnikaCoworking.adapter.repository.NotificationRepository;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.MessageResponse;
import com.example.BrusnikaCoworking.adapter.web.user.dto.profile.Profile;
import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.NotificationForm;
import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.ReservalProfile;
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
public class ProfileService {
    private final NotificationRepository notificationRepository;
    private final ReservalService reservalService;

    public Profile getProfile(UserEntity user) {
        List<ReservalProfile> reservals = new ArrayList<>();
        var reservalsEntity = reservalService.reservalsActiveUser(user);
        for (var item : reservalsEntity) {
            var reserval = new ReservalProfile(
                    item.getId_reserval(),
                    DateTimeFormatter.ofPattern("dd.MM.YYYY").format(item.getDate()),
                    DateTimeFormatter.ofPattern("HH:mm").format(item.getTimeStart()),
                    DateTimeFormatter.ofPattern("HH:mm").format(item.getTimeEnd()));
            reservals.add(reserval);
        }
        return new Profile(user.getUsername(), user.getRealname(), reservals);
    }

    public MessageResponse confirmGroupReserval(Long id) {
        var notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        return reservalService.updateStateGroup(notification.getReserval());
    }

    public List<NotificationForm> allNotification (UserEntity user) {
        List<NotificationForm> notifications = new ArrayList<>();
        var notificationsEntity = notificationRepository.findByUserOrderBySendTimeDesc(user);
        for(var item : notificationsEntity) {
            var reserval = item.getReserval();
            var invit = "";
            if (!reserval.getStateGroup().equals(State.FALSE)) invit = reserval.getInvit().getUsername();
            var form = new NotificationForm(
                    item.getId_notif(),
                    DateTimeFormatter.ofPattern("dd.MM.YYYY").format(reserval.getDate()),
                    DateTimeFormatter.ofPattern("HH:mm").format(reserval.getTimeStart()),
                    DateTimeFormatter.ofPattern("HH:mm").format(reserval.getTimeEnd()),
                    DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm").format(item.getSendTime()),
                    reserval.getStateGroup(),
                    invit);
            notifications.add(form);
        }
        return notifications;
    }
}
