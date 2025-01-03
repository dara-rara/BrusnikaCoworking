package com.example.BrusnikaCoworking.domain.reserval;

import com.example.BrusnikaCoworking.domain.coworking.CoworkingEntity;
import com.example.BrusnikaCoworking.domain.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.time.LocalTime;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@Setter
@Getter
@Entity
@Table(name = "Reservals")
@FieldNameConstants
@NoArgsConstructor
public class ReservalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_reserval;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_table", nullable = false)
    private CoworkingEntity table;
    @Temporal(TemporalType.TIME)
    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;
    @Temporal(TemporalType.TIME)
    @Column(name = "time_end", nullable = false)
    private LocalTime timeEnd;
    @Temporal(TemporalType.DATE)
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    @Column(name = "state_reserval", nullable = false)
    private State stateReserval;//активная, подтвержденная, неподтвержденная или отмененная бронь
    @Enumerated(EnumType.STRING)
    @Column(name = "state_group", nullable = false)
    private State stateGroup;//наличие группы(при наличии группы нужно подтвержденние)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_invit")
    private UserEntity invit;

}
