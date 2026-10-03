package com.example.dbserver.service;

import com.example.dbserver.entity.Alarm;
import com.example.dbserver.entity.Ingredient;
import com.example.dbserver.entity.User;
import com.example.dbserver.repository.AlarmRepository;
import com.example.dbserver.repository.IngredientRepository;
import com.example.dbserver.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlarmServiceTest {

    @Test
    void createIngredientAlarms_calculatesSevenThreeAndSameDay() {
        AlarmRepository alarmRepository = mock(AlarmRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        IngredientRepository ingredientRepository = mock(IngredientRepository.class);
        EmailService emailService = mock(EmailService.class);

        AlarmService alarmService = new AlarmService(
                alarmRepository, userRepository, ingredientRepository, emailService
        );

        User user = new User();
        user.setUserId(3);

        Ingredient ingredient = new Ingredient();
        ingredient.setId(10);
        ingredient.setUser(user);
        ingredient.setExpirationDate(LocalDate.of(2026, 10, 20));

        when(alarmRepository.existsByIngredientId(10)).thenReturn(false);

        alarmService.createIngredientAlarms(ingredient);

        verify(alarmRepository).save(argThat(alarm -> {
            assertEquals(LocalDate.of(2026, 10, 13), alarm.getAlarmDate7());
            assertEquals(LocalDate.of(2026, 10, 17), alarm.getAlarmDate3());
            assertEquals(LocalDate.of(2026, 10, 20), alarm.getAlarmDate0());
            return true;
        }));
    }

    @Test
    void createIngredientAlarms_doesNotDuplicateExistingAlarm() {
        AlarmRepository alarmRepository = mock(AlarmRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        IngredientRepository ingredientRepository = mock(IngredientRepository.class);
        EmailService emailService = mock(EmailService.class);

        AlarmService alarmService = new AlarmService(
                alarmRepository, userRepository, ingredientRepository, emailService
        );

        User user = new User();
        user.setUserId(3);

        Ingredient ingredient = new Ingredient();
        ingredient.setId(10);
        ingredient.setUser(user);
        ingredient.setExpirationDate(LocalDate.of(2026, 10, 20));

        when(alarmRepository.existsByIngredientId(10)).thenReturn(true);

        alarmService.createIngredientAlarms(ingredient);

        verify(alarmRepository, never()).save(any(Alarm.class));
    }
}
