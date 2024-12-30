package com.example.BrusnikaCoworking.domain.notification;

import com.example.BrusnikaCoworking.domain.reserval.ReservalEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@Setter
@Getter
@Entity
@Table(name = "Task")
@FieldNameConstants
@NoArgsConstructor
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_task;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_reserval", nullable = false)
    private ReservalEntity reserval;
    @Column(name = "send_time", nullable = false)
    private LocalDateTime sendTime;
}
