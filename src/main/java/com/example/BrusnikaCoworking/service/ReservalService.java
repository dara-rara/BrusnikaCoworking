package com.example.BrusnikaCoworking.service;

import com.example.BrusnikaCoworking.adapter.repository.CodeRepository;
import com.example.BrusnikaCoworking.adapter.repository.CoworkingRepository;
import com.example.BrusnikaCoworking.adapter.repository.NotificationRepository;
import com.example.BrusnikaCoworking.adapter.repository.ReservalRepository;
import com.example.BrusnikaCoworking.adapter.web.admin.dto.reserval.Date;
import com.example.BrusnikaCoworking.adapter.web.admin.dto.reserval.ReservalActiveDate;
import com.example.BrusnikaCoworking.adapter.web.admin.dto.reserval.ReservalAdminForm;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.MessageResponse;
import com.example.BrusnikaCoworking.adapter.web.auth.dto.mail.KafkaMailMessage;
import com.example.BrusnikaCoworking.adapter.web.user.dto.notification.NotificationAndReserval;
import com.example.BrusnikaCoworking.adapter.web.user.dto.reserval.*;
import com.example.BrusnikaCoworking.adapter.web.user.dto.notification.ReservalActive;
import com.example.BrusnikaCoworking.config.kafka.KafkaProducer;
import com.example.BrusnikaCoworking.domain.notification.NotificationEntity;
import com.example.BrusnikaCoworking.domain.notification.Type;
import com.example.BrusnikaCoworking.domain.reserval.ReservalEntity;
import com.example.BrusnikaCoworking.domain.reserval.State;
import com.example.BrusnikaCoworking.domain.reserval.TypeDesing;
import com.example.BrusnikaCoworking.domain.user.Role;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final CodeRepository codeRepository;
    private static final String EMAIL_TOPIC_GR = "email_message_reserval_group";
    private static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");

    public ReservalDateCategories reservalsAllUser(UserEntity user) {
        var now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        List<ReservalEntity> reservalsNew = reservalRepository.findByUserAndStateReservalOrderByDateDescTimeStartDesc(user, State.TRUE);

        Map<String, List<Reserval>> categorizedReservals = new HashMap<>();
        categorizedReservals.put("today", new ArrayList<>());
        categorizedReservals.put("last7Days", new ArrayList<>());
        categorizedReservals.put("lastMonth", new ArrayList<>());

        for (var reserval : reservalsNew) {
            String invitUsername = reserval.getInvit() != null ? reserval.getInvit().getUsername() : "";

//            State state = reserval.getDate().equals(today)
//                    && currentTime.isAfter(reserval.getTimeStart())
//                    && currentTime.isBefore(reserval.getTimeEnd())
//                    && reserval.getStateGroup().equals(State.FALSE) ? State.EXPECTATION : reserval.getStateReserval();

            TypeDesing typeDesing;
            if (reserval.getStateGroup().equals(State.TRUE)) typeDesing = TypeDesing.GROUP;
            else if (reserval.getDate().equals(today) && currentTime.isAfter(reserval.getTimeStart().minusHours(2))
            && currentTime.isBefore(reserval.getTimeStart())) typeDesing = TypeDesing.ACTIVE_TWO_HOUR;
            else if (reserval.getDate().equals(today) && currentTime.isAfter(reserval.getTimeStart())
            && currentTime.isBefore(reserval.getTimeEnd())) typeDesing = TypeDesing.EXPECTATION_CODE;
            else typeDesing = TypeDesing.ACTIVE;

            Reserval form = new Reserval(
                    reserval.getId_reserval(),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy").format(reserval.getDate()),
                    DateTimeFormatter.ofPattern("HH:mm").format(reserval.getTimeStart()),
                    DateTimeFormatter.ofPattern("HH:mm").format(reserval.getTimeEnd()),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(reserval.getSendTime()),
                    reserval.getTable().getNumber(),
                    typeDesing,
                    invitUsername
            );

            if (reserval.getSendTime().toLocalDate().isEqual(now.toLocalDate())) {
                categorizedReservals.get("today").add(form);
            } else if (reserval.getSendTime().isAfter(now.minusDays(7))) {
                categorizedReservals.get("last7Days").add(form);
            } else {
                categorizedReservals.get("lastMonth").add(form);
            }
        }

        List<Reserval> categoriesOld = new ArrayList<>();
        List<ReservalEntity> reservalsOld = reservalRepository.findByUserAndStateReservalNotOrderByDateDescTimeStartDesc(user, State.TRUE);
        for (var reserval : reservalsOld) {
            if (!reserval.getStateReserval().equals(State.VERIFIED)) {
                String invitUsername = reserval.getInvit() != null ? reserval.getInvit().getUsername() : "";
                TypeDesing typeDesing;
                if (reserval.getStateReserval().equals(State.CONFIRMED)) typeDesing = TypeDesing.CONFIRMED;
                else typeDesing = TypeDesing.UNCONFIRMED;

                Reserval form = new Reserval(
                        reserval.getId_reserval(),
                        DateTimeFormatter.ofPattern("dd.MM.yyyy").format(reserval.getDate()),
                        DateTimeFormatter.ofPattern("HH:mm").format(reserval.getTimeStart()),
                        DateTimeFormatter.ofPattern("HH:mm").format(reserval.getTimeEnd()),
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(reserval.getSendTime()),
                        reserval.getTable().getNumber(),
                        typeDesing,
                        invitUsername
                );

                categoriesOld.add(form);
            }
        }
        return new ReservalDateCategories(categorizedReservals.get("today"),
                categorizedReservals.get("last7Days"),
                categorizedReservals.get("lastMonth"),
                categoriesOld);
    }

    public MessageResponse cancelReserval(Long id, UserEntity user) {
        var optional = reservalRepository.findById(id);
        if (optional.isEmpty()) throw new ReservalException("reserval not found");
        var reserval = optional.get();
        if (!(reserval.getUser().getUsername().equals(user.getUsername())
                || user.getRole().equals(Role.ADMIN)))
            throw new ResourceException("no rights to cancel a reserval");
        var currentDate = LocalDate.now();
        var currentTime = LocalTime.now();
        if ((reserval.getDate().isEqual(currentDate) && reserval.getTimeStart().isBefore(currentTime))
                || reserval.getDate().isBefore(currentDate))
            throw new ReservalException("it is not possible to cancel during or after reserval");
        reserval.setStateReserval(State.FALSE);
        reservalRepository.save(reserval);
        var notification = new NotificationEntity();
        notification.setSendTime(LocalDateTime.now());
        notification.setReserval(reserval);
        notification.setUser(user);
        notification.setType(Type.CANCEL);
        notification.setState(State.FALSE);
        notification.setTitle("Отмена бронирования");
        notification.setText("Вы отменили бронирование на " + reserval.getDate()
                .format(DateTimeFormatter.ofPattern("dd.MM.yy")) + ".");
        notificationRepository.save(notification);
        if (reserval.getStateGroup().equals(State.CONFIRMED)) {
            var notificationInvit = new NotificationEntity();
            notificationInvit.setSendTime(LocalDateTime.now());
            notificationInvit.setReserval(reserval);
            notificationInvit.setUser(reserval.getInvit());
            notificationInvit.setType(Type.CANCEL);
            notificationInvit.setState(State.FALSE);
            notificationInvit.setTitle("Отмена бронирования");
            notificationInvit.setText("Пользователь " + user.getUsername() + " отменил бронирование на " + reserval.getDate()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yy")) + ".");
            notificationRepository.save(notificationInvit);
        }
        return new MessageResponse("reserval cancelled");
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
        try {
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
        } catch (Exception e) {
            throw new ResourceException("not valid date");
        }
    }

    public List<ReservalActive> reservalsActiveUser(UserEntity user) {
        List<ReservalActive> reservals = new ArrayList<>();
        var now = LocalDateTime.now();
        var reservalsEntity =
                reservalRepository.findByUserAndStateReservalAndDateAfterOrTimeEndAfter(
                        user.getId_user(), now.toLocalDate(), now.toLocalTime());
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

    public MessageResponse updateStateCode(ReservalEntity reserval, Code response) {
        var opt = codeRepository.findTopByOrderBySendTimeDesc();
        String code = null;
        if (opt.isPresent()) code = opt.get().getCode();
        var now = LocalDateTime.now();
        if (reserval.getStateReserval().equals(State.TRUE)){
            if (now.toLocalTime().isBefore(reserval.getTimeEnd())
                    && reserval.getDate().isEqual(now.toLocalDate())) {
                if (code.equals(response.code())) {
                    reserval.setStateReserval(State.CONFIRMED);
                    reservalRepository.save(reserval);
                    var notification = new NotificationEntity();
                    notification.setSendTime(LocalDateTime.now());
                    notification.setReserval(reserval);
                    notification.setUser(reserval.getUser());
                    notification.setType(Type.CODE);
                    notification.setState(State.FALSE);
                    notification.setTitle("Подтверждение присутствия");
                    notification.setText("Мы рады, что Вы пришли! Ваше бронирование подтверждено.");
                    notificationRepository.save(notification);
                    return new MessageResponse("reserval confirmed");
                }
                throw new ResourceException("the code is incorrect");
            }
            throw new ResourceException("the reserval has ended");
        } else if (reserval.getStateReserval().equals(State.CONFIRMED))
            throw new ResourceException("the reserval has already been confirmed");
        throw new ResourceException("the reserval was cancelled");
    }

    public MessageResponse confirmGroupReserval(Long id) {
        var reserval = reservalRepository.findById(id)
                .orElseThrow(() -> new ResourceException("reserval not found"));
        return updateStateGroup(reserval, State.CONFIRMED);
    }

    public MessageResponse unconfirmGroupReserval(Long id) {
        var reserval = reservalRepository.findById(id)
                .orElseThrow(() -> new ResourceException("reserval not found"));
        return updateStateGroup(reserval, State.UNCONFIRMED);
    }

    public MessageResponse updateStateGroup(ReservalEntity reserval, State state) {
        if (reserval.getStateGroup().equals(State.TRUE) && state.equals(State.CONFIRMED)) {
            reserval.setStateGroup(State.CONFIRMED);
            reservalRepository.save(reserval);
            var notification = new NotificationEntity();
            notification.setSendTime(LocalDateTime.now());
            notification.setReserval(reserval);
            notification.setUser(reserval.getUser());
            notification.setType(Type.CREATE);
            notification.setState(State.FALSE);
            notification.setTitle("Бронирование");
            notification.setText("Вы забронировали коворкинг на " + reserval.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yy")) +
                    " с " + reserval.getTimeStart().format(DateTimeFormatter.ofPattern("HH:mm")) +
                    " до " + reserval.getTimeEnd().format(DateTimeFormatter.ofPattern("HH:mm")) + "! Место №" +
                    reserval.getTable().getNumber().toString());
            notificationRepository.save(notification);
            var notificationInvit = new NotificationEntity();
            notificationInvit.setSendTime(LocalDateTime.now());
            notificationInvit.setReserval(reserval);
            notificationInvit.setUser(reserval.getInvit());
            notificationInvit.setType(Type.GROUP);
            notificationInvit.setState(State.FALSE);
            notificationInvit.setTitle("Подтверждение приглашения");
            notificationInvit.setText("Пользователь " + reserval.getUser().getUsername() + " принял Ваше приглашение.");
            notificationRepository.save(notificationInvit);
            return new MessageResponse("reserval confirmed");
        }
        else if (reserval.getStateGroup().equals(State.TRUE) && state.equals(State.UNCONFIRMED)) {
            reserval.setStateGroup(State.UNCONFIRMED);
            reserval.setStateReserval(State.FALSE);
            reservalRepository.save(reserval);
            var notificationInvit = new NotificationEntity();
            notificationInvit.setSendTime(LocalDateTime.now());
            notificationInvit.setReserval(reserval);
            notificationInvit.setUser(reserval.getInvit());
            notificationInvit.setType(Type.GROUP);
            notificationInvit.setState(State.FALSE);
            notificationInvit.setTitle("Подтверждение приглашения");
            notificationInvit.setText("Пользователь " + reserval.getUser().getUsername() + " не принял Ваше приглашение.");
            notificationRepository.save(notificationInvit);
            return new MessageResponse("reserval unconfirmed");
        }
        else if (reserval.getStateGroup().equals(State.CONFIRMED))
            throw new ResourceException("the reserval has already been confirmed");
        else if (reserval.getStateGroup().equals(State.UNCONFIRMED))
            throw new ResourceException("the reserval has already been unconfirmed");
        else throw new ResourceException("the reserval is not group");
    }

    public List<Integer> getBusyTables(DateAndTime dateAndTime) {
        try {
            return coworkingRepository.findByNotReservalTable(
                    new SimpleDateFormat("dd.MM.yyyy").parse(dateAndTime.date()),
                    new SimpleDateFormat("HH:mm").parse(dateAndTime.timeStart()),
                    new SimpleDateFormat("HH:mm").parse(dateAndTime.timeEnd()));
        } catch (Exception e) {
            throw new ResourceException("not valid date or time");
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
                    throw new ResourceException("not valid date or time");
                }
                reserval.setStateReserval(State.ADMIN);
                reserval.setStateGroup(State.FALSE);
                reservalRepository.save(reserval);
            }
            return new MessageResponse("reserval created");
        }
        throw new ReservalException("the table is occupied");
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
                    throw new ReservalException("the user " + userReserval.getUsername() +
                            " already has a reserval for this time");
                reserval.setUser(userReserval);
                reserval.setTable(table);
                reserval.setSendTime(now);
                try {
                    reserval.setTimeStart(LocalTime.parse(form.timeStart(), formatterTime));
                    reserval.setTimeEnd(LocalTime.parse(form.timeEnd(), formatterTime));
                    reserval.setDate(LocalDate.parse(form.date(), formatterDate));
                } catch (Exception e) {
                    throw new ResourceException("not valid date or time");
                }
                reserval.setStateReserval(State.TRUE);
                if (form.usernames().size() > 1) {
                    if (user.getUsername().equals(form.usernames().get(i))) {
                        reserval.setStateGroup(State.FALSE);
                        reservalRepository.save(reserval);
                        var notification = new NotificationEntity();
                        notification.setSendTime(now);
                        notification.setReserval(reserval);
                        notification.setUser(user);
                        notification.setType(Type.CREATE);
                        notification.setState(State.FALSE);
                        notification.setTitle("Бронирование");
                        notification.setText("Вы забронировали коворкинг на " + reserval.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yy")) +
                                " с " + reserval.getTimeStart().format(DateTimeFormatter.ofPattern("HH:mm")) +
                                " до " + reserval.getTimeEnd().format(DateTimeFormatter.ofPattern("HH:mm")) + "! Место №" +
                                reserval.getTable().getNumber().toString());
                        notificationRepository.save(notification);
                    } else {
                        reserval.setStateGroup(State.TRUE);
                        reserval.setInvit(user);
                        reservalRepository.save(reserval);
                        var notification = new NotificationEntity();
                        notification.setSendTime(now);
                        notification.setReserval(reserval);
                        notification.setUser(userReserval);
                        notification.setType(Type.GROUP);
                        notification.setState(State.FALSE);
                        notification.setTitle("Приглашение");
                        notification.setText("Пользователь " + user.getUsername() + " забронировал для Вас место №" +
                                reserval.getTable().getNumber().toString() + " в коворкинге на " +
                                reserval.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yy")) +
                                ". Подтвердите в разделе Бронирования в течение суток.");
                        notificationRepository.save(notification);
                        var notificationInvit = new NotificationEntity();
                        notificationInvit.setSendTime(now);
                        notificationInvit.setReserval(reserval);
                        notificationInvit.setUser(user);
                        notificationInvit.setType(Type.GROUP);
                        notificationInvit.setState(State.FALSE);
                        notificationInvit.setTitle("Отправка приглашения");
                        notificationInvit.setText("Вы отправили приглашение пользователю " +
                                userReserval.getUsername() + ".");
                        notificationRepository.save(notificationInvit);
                        reservalGroupNotification(reserval);
                    }
                } else {
                    reserval.setStateGroup(State.FALSE);
                    reservalRepository.save(reserval);
                    var notification = new NotificationEntity();
                    notification.setSendTime(now);
                    notification.setReserval(reserval);
                    notification.setUser(userReserval);
                    notification.setType(Type.CREATE);
                    notification.setState(State.FALSE);
                    notification.setTitle("Бронирование");
                    notification.setText("Вы забронировали коворкинг на " + reserval.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yy")) +
                            " с " + reserval.getTimeStart().format(DateTimeFormatter.ofPattern("HH:mm")) +
                            " до " + reserval.getTimeEnd().format(DateTimeFormatter.ofPattern("HH:mm")) + "! Место №" +
                            reserval.getTable().getNumber().toString() + ".");
                    notificationRepository.save(notification);
                }
                taskService.scheduleNotificationCode(reserval,
                        LocalDateTime.of(reserval.getDate(), reserval.getTimeStart()));
                taskService.scheduleNotificationMemento(reserval,
                        LocalDateTime.of(reserval.getDate(), reserval.getTimeStart()));

                i++;
            }
            return new MessageResponse("reserval created");
        }
        throw new ReservalException("the table is occupied");
    }

    private boolean checkReserval(DateAndTime dateAndTime, UserEntity user) {
        try {
            return !reservalRepository.findActiveReservalsInTimeRangeForUser(
                    new SimpleDateFormat("dd.MM.yyyy").parse(dateAndTime.date()),
                    new SimpleDateFormat("HH:mm").parse(dateAndTime.timeStart()),
                    new SimpleDateFormat("HH:mm").parse(dateAndTime.timeEnd()),
                    user.getId_user()).isEmpty();
        } catch (Exception e) {
            throw new ResourceException("not valid date or time");
        }
    }

    private boolean checkTable(List<Integer> table, List<Integer> busyTable) {
        return !busyTable.containsAll(table);
    }
}
