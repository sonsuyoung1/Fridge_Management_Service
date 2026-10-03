package com.example.dbserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alarm")
@Getter
@Setter
@NoArgsConstructor
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long alarmId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "ingredient_id", nullable = false, unique = true)
    private Integer ingredientId;

    @Column(name = "alarm_date_7")
    private LocalDate alarmDate7;

    @Column(name = "alarm_date_3")
    private LocalDate alarmDate3;

    @Column(name = "alarm_date_0")
    private LocalDate alarmDate0;

    @Column(name = "is_sent_7", nullable = false)
    private Boolean isSent7 = false;

    @Column(name = "is_sent_3", nullable = false)
    private Boolean isSent3 = false;

    @Column(name = "is_sent_0", nullable = false)
    private Boolean isSent0 = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
