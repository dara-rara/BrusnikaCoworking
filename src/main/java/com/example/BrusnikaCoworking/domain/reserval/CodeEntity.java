package com.example.BrusnikaCoworking.domain.reserval;

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
@Table(name = "Code")
@FieldNameConstants
@NoArgsConstructor
public class CodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_code;
    @Column(name = "send_time", nullable = false)
    private LocalDateTime sendTime;
    @Column(name = "code", nullable = false)
    private String code;
}
