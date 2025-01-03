package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.repository.CoworkingRepository;
import com.example.BrusnikaCoworking.adapter.repository.NotificationRepository;
import com.example.BrusnikaCoworking.adapter.repository.ReservalRepository;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.MessageResponse;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.KafkaMailMessage;
import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.DateAndTime;
import com.example.BrusnikaCoworking.adapter.web.user.dto.notification.ReservalActive;
import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.ReservalForm;
import com.example.BrusnikaCoworking.config.kafka.KafkaProducer;
import com.example.BrusnikaCoworking.domain.notification.NotificationEntity;
import com.example.BrusnikaCoworking.domain.notification.Type;
import com.example.BrusnikaCoworking.domain.reserval.ReservalEntity;
import com.example.BrusnikaCoworking.domain.reserval.State;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import com.example.BrusnikaCoworking.exception.EmailRegisteredException;
import com.example.BrusnikaCoworking.exception.ReservalExistException;
import com.example.BrusnikaCoworking.service.scheduled.TaskService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ReservalService {
    private final ReservalRepository reservalRepository;
    private final CoworkingRepository coworkingRepository;
    private final NotificationRepository notificationRepository;
    private final TaskService taskService;
    private final UserService userService;
    private final KafkaProducer kafkaProducer;
    private static final String EMAIL_TOPIC_GR = "email_message_reserval_group";


    public List<ReservalActive> reservalsActiveUser(UserEntity user) {
        List<ReservalActive> reservals = new ArrayList<>();
        var reservalsEntity =
                reservalRepository.findByUserAndStateReservalOrderByDateDesc(user, State.TRUE);
        for (var item : reservalsEntity) {
            var reserval = new ReservalActive(
                    item.getId_reserval(),
                    DateTimeFormatter.ofPattern("dd.MM.YYYY").format(item.getDate()),
                    DateTimeFormatter.ofPattern("HH:mm").format(item.getTimeStart()),
                    DateTimeFormatter.ofPattern("HH:mm").format(item.getTimeEnd()),
                    item.getTable().getNumber()
            );
            reservals.add(reserval);
        }
        return reservals;
    }

    public void reservalGroupNotification(ReservalEntity reserval) {
        try {
            if (!reservalRepository.existsById(reserval.getId_reserval())) {
                throw new EmailRegisteredException("reserval: %s not found".formatted(reserval.getId_reserval()));
            }
            kafkaProducer.produce(EMAIL_TOPIC_GR, new KafkaMailMessage(reserval.getUser().getUsername(), ""));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public MessageResponse updateStateGroup(ReservalEntity reserval) {
        if (reserval.getStateGroup().equals(State.TRUE)) {
            reserval.setStateGroup(State.CONFIRMED);
            reservalRepository.save(reserval);
            return new MessageResponse("Reserval confirmed");
        }
        else if (reserval.getStateGroup().equals(State.CONFIRMED))
            return new MessageResponse("The reserval has already been confirmed");
        else return new MessageResponse("The reserval is not group");
    }

    public List<Integer> getFreeTables(DateAndTime dateAndTime) throws ParseException {
        return coworkingRepository.findByNotReservalTable(
                new SimpleDateFormat("dd.MM.yyyy").parse(dateAndTime.date()),
                new SimpleDateFormat("HH:mm").parse(dateAndTime.timeStart()),
                new SimpleDateFormat("HH:mm").parse(dateAndTime.timeEnd()));
    }

    //нужна проверка, чтоб пользователь не смог забронить дважды на одно и то же время
    public boolean createReserval(ReservalForm form, UserEntity user) throws ParseException {
        var dateForm = new DateAndTime(form.date(), form.timeStart(), form.timeEnd());
        var freeTables = getFreeTables(dateForm);
        var i = 0;
        if (checkTable(form.tables(), freeTables)) {
            for (var item : form.tables()) {
                var table = coworkingRepository.findByNumber(item);
                var reserval = new ReservalEntity();
                var userReserval = userService.getByUsername(form.usernames().get(i));
                if(checkReserval(dateForm, userReserval))
                    throw new ReservalExistException("The user " + userReserval.getUsername() +
                            " already has a reserval for this time");
                reserval.setUser(userReserval);
                reserval.setTable(table);
                reserval.setTimeStart(LocalTime.parse(form.timeStart()));
                reserval.setTimeEnd(LocalTime.parse(form.timeEnd()));
                var date = new SimpleDateFormat("dd.MM.yyyy").parse(form.date());
                reserval.setDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                reserval.setStateReserval(State.TRUE);
                if (form.usernames().size() > 1) {
                    if (user.getUsername().equals(form.usernames().get(i))) {
                        reserval.setStateGroup(State.FALSE);
                        reservalRepository.save(reserval);
                    } else {
                        reserval.setStateGroup(State.TRUE);
                        reserval.setInvit(user);
                        reservalRepository.save(reserval);
                        var notification = new NotificationEntity();
                        notification.setSendTime(LocalDateTime.of(reserval.getDate(), reserval.getTimeStart()));
                        notification.setReserval(reserval);
                        notification.setUser(userReserval);
                        notification.setType(Type.GROUP);
                        notificationRepository.save(notification);
                        reservalGroupNotification(reserval);
                    }
                } else {
                    reserval.setStateGroup(State.FALSE);
                    reservalRepository.save(reserval);
                }
                taskService.scheduleNotificationCode(reserval,
                        LocalDateTime.of(reserval.getDate(), reserval.getTimeStart()));
                taskService.scheduleNotificationMemento(reserval,
                        LocalDateTime.of(reserval.getDate(), reserval.getTimeStart()));

                i++;
            }
            return true;
        }
        return false;
    }

    private boolean checkReserval(DateAndTime dateAndTime, UserEntity user) throws ParseException {

        return !reservalRepository.findActiveReservalsInTimeRangeForUser(
                new SimpleDateFormat("dd.MM.yyyy").parse(dateAndTime.date()),
                new SimpleDateFormat("HH:mm").parse(dateAndTime.timeStart()),
                new SimpleDateFormat("HH:mm").parse(dateAndTime.timeEnd()),
                user.getId_user()).isEmpty();
    }

    private boolean checkTable(List<Integer> table, List<Integer> freeTables) {
        return freeTables.containsAll(table);
    }
}
