package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.repository.CoworkingRepository;
import com.example.BrusnikaCoworking.adapter.repository.NotificationRepository;
import com.example.BrusnikaCoworking.adapter.repository.ReservalRepository;
import com.example.BrusnikaCoworking.adapter.web.admin.dto.reserval.Date;
import com.example.BrusnikaCoworking.adapter.web.admin.dto.reserval.ReservalActiveDate;
import com.example.BrusnikaCoworking.adapter.web.admin.dto.reserval.ReservalAdminForm;
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
import com.example.BrusnikaCoworking.exception.EmailException;
import com.example.BrusnikaCoworking.exception.InternalServerErrorException;
import com.example.BrusnikaCoworking.exception.ReservalException;
import com.example.BrusnikaCoworking.exception.ResourceException;
import com.example.BrusnikaCoworking.service.scheduled.TaskService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
    private static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");

    public void cancelReserval(Long id) {
        var optional = reservalRepository.findById(id);
        if (optional.isEmpty()) throw new ReservalException("reserval not found");
        var reserval = optional.get();
        var y = reserval.getUser();
        var currentDate = LocalDate.now();
        var currentTime = LocalTime.now();
        if ((reserval.getDate().isEqual(currentDate) && reserval.getTimeStart().isBefore(currentTime))
                || reserval.getDate().isBefore(currentDate))
            throw new ReservalException("it is not possible to cancel during or after reserval");
        reserval.setStateReserval(State.FALSE);
    }

    public void cancelReservalAdmin(Long id) {
        var optional = reservalRepository.findById(id);
        if (optional.isEmpty()) throw new ReservalException("reserval not found");
        var reserval = optional.get();
        var currentDate = LocalDate.now();
        var currentTime = LocalTime.now();
        if ((reserval.getDate().isEqual(currentDate) && reserval.getTimeStart().isBefore(currentTime))
                || reserval.getDate().isBefore(currentDate))
            throw new ReservalException("it is not possible to cancel during or after reserval");
        reserval.setStateReserval(State.FALSE);
    }

    public List<ReservalActiveDate> reservalsActiveUserDate(Date date) {
        List<ReservalActiveDate> reservals = new ArrayList<>();
        var reservalsEntity =
                reservalRepository.findByDateAndStateReservalOrderByTimeStart(
                        LocalDate.parse(date.date(), formatterDate));
        for (var item : reservalsEntity) {
            var reserval = new ReservalActiveDate(
                    item.getId_reserval(),
                    item.getUser().getUsername(),
                    item.getUser().getRealname(),
                    DateTimeFormatter.ofPattern("dd.MM.YYYY").format(item.getDate()),
                    DateTimeFormatter.ofPattern("HH:mm").format(item.getTimeStart()),
                    DateTimeFormatter.ofPattern("HH:mm").format(item.getTimeEnd()),
                    item.getTable().getNumber()
            );
            reservals.add(reserval);
        }
        return reservals;
    }

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
        if (!reservalRepository.existsById(reserval.getId_reserval())) {
            throw new EmailException("reserval: %s not found".formatted(reserval.getId_reserval()));
        }
        kafkaProducer.produce(EMAIL_TOPIC_GR, new KafkaMailMessage(reserval.getUser().getUsername(), ""));
    }

    public MessageResponse updateStateGroup(ReservalEntity reserval) {
        if (reserval.getStateGroup().equals(State.TRUE)) {
            reserval.setStateGroup(State.CONFIRMED);
            reservalRepository.save(reserval);
            return new MessageResponse("Reserval confirmed");
        }
        else if (reserval.getStateGroup().equals(State.CONFIRMED))
            throw new ResourceException("The reserval has already been confirmed");
        else throw new ResourceException("The reserval is not group");
    }

    public List<Integer> getBusyTables(DateAndTime dateAndTime) {
        try {
            return coworkingRepository.findByNotReservalTable(
                    new SimpleDateFormat("dd.MM.yyyy").parse(dateAndTime.date()),
                    new SimpleDateFormat("HH:mm").parse(dateAndTime.timeStart()),
                    new SimpleDateFormat("HH:mm").parse(dateAndTime.timeEnd()));
        } catch (Exception e) {
            throw new ResourceException("Not valid date or time");
        }
    }

    public MessageResponse createAdminReserval(ReservalAdminForm form, UserEntity user) {
        var dateForm = new DateAndTime(form.date(), form.timeStart(), form.timeEnd());
        var freeTables = getBusyTables(dateForm);
        var now = LocalDateTime.now();
        if (checkTable(form.tables(), freeTables)) {
            for (var item : form.tables()) {
                var table = coworkingRepository.findByNumber(item);
                var reserval = new ReservalEntity();
                reserval.setUser(user);
                reserval.setTable(table);
                reserval.setSendTime(now);
                try {
                    reserval.setTimeStart(LocalTime.parse(form.timeStart(), formatterTime));
                    reserval.setTimeEnd(LocalTime.parse(form.timeEnd(), formatterTime));
                    reserval.setDate(LocalDate.parse(form.date(), formatterDate));
                } catch (Exception e) {
                    throw new ResourceException("Not valid date or time");
                }
                reserval.setStateReserval(State.ADMIN);
                reserval.setStateGroup(State.FALSE);
                reservalRepository.save(reserval);
            }
            return new MessageResponse("Reserval created");
        }
        throw new ReservalException("The table is occupied");
    }

    public MessageResponse createReserval(ReservalForm form, UserEntity user) {
        var dateForm = new DateAndTime(form.date(), form.timeStart(), form.timeEnd());
        var freeTables = getBusyTables(dateForm);
        var i = 0;
        var now = LocalDateTime.now();
        if (form.tables().size() != form.usernames().size())
            throw new ReservalException("the number of tables and users is not equal");
        if (checkTable(form.tables(), freeTables)) {
            for (var item : form.tables()) {
                var table = coworkingRepository.findByNumber(item);
                var reserval = new ReservalEntity();
                var userReserval = userService.getByUsername(form.usernames().get(i));
                if (checkReserval(dateForm, userReserval))
                    throw new ReservalException("The user " + userReserval.getUsername() +
                            " already has a reserval for this time");
                reserval.setUser(userReserval);
                reserval.setTable(table);
                reserval.setSendTime(now);
                try {
                    reserval.setTimeStart(LocalTime.parse(form.timeStart(), formatterTime));
                    reserval.setTimeEnd(LocalTime.parse(form.timeEnd(), formatterTime));
                    reserval.setDate(LocalDate.parse(form.date(), formatterDate));
                } catch (Exception e) {
                    throw new ResourceException("Not valid date or time");
                }
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
                        notification.setSendTime(now);
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
            return new MessageResponse("Reserval created");
        }
        throw new ReservalException("The table is occupied");
    }

    private boolean checkReserval(DateAndTime dateAndTime, UserEntity user) {
        try {
            return !reservalRepository.findActiveReservalsInTimeRangeForUser(
                    new SimpleDateFormat("dd.MM.yyyy").parse(dateAndTime.date()),
                    new SimpleDateFormat("HH:mm").parse(dateAndTime.timeStart()),
                    new SimpleDateFormat("HH:mm").parse(dateAndTime.timeEnd()),
                    user.getId_user()).isEmpty();
        } catch (Exception e) {
            throw new ResourceException("Not valid date or time");
        }
    }

    private boolean checkTable(List<Integer> table, List<Integer> busyTable) {
        return !busyTable.containsAll(table);
    }
}
