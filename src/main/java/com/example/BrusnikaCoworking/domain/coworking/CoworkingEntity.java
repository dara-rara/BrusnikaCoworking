package com.example.BrusnikaCoworking.domain.coworking;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;

import static lombok.AccessLevel.PRIVATE;

@FieldDefaults(level = PRIVATE)
@Setter
@Getter
@Entity
@Table(name = "Coworking")
@FieldNameConstants
@NoArgsConstructor
public class CoworkingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_table;
    @Column(name = "number", nullable = false, unique = true)
    private Integer number;
}
