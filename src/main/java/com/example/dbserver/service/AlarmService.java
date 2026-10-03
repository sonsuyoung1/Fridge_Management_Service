package com.example.dbserver.service;

import com.example.dbserver.entity.Alarm;
import com.example.dbserver.entity.Ingredient;
import com.example.dbserver.entity.User;
import com.example.dbserver.repository.AlarmRepository;
import com.example.dbserver.repository.IngredientRepository;
import com.example.dbserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final EmailService emailService;

    @Transactional
    public void createIngredientAlarms(Ingredient ingredient) {
        if (alarmRepository.existsByIngredientId(ingredient.getId())) {
            return;
        }

        LocalDate expirationDate = ingredient.getExpirationDate();

        Alarm alarm = new Alarm();
        alarm.setUserId(ingredient.getUser().getUserId());
        alarm.setIngredientId(ingredient.getId());
        alarm.setAlarmDate7(expirationDate.minusDays(7));
        alarm.setAlarmDate3(expirationDate.minusDays(3));
        alarm.setAlarmDate0(expirationDate);
        alarm.setIsSent7(false);
        alarm.setIsSent3(false);
        alarm.setIsSent0(false);
        alarm.setCreatedAt(LocalDateTime.now());

        alarmRepository.save(alarm);
    }

    @Transactional
    public void updateIngredientAlarms(Ingredient ingredient) {
        Alarm alarm = alarmRepository.findByIngredientId(ingredient.getId());
        if (alarm == null) {
            createIngredientAlarms(ingredient);
            return;
        }

        LocalDate expirationDate = ingredient.getExpirationDate();

        alarm.setAlarmDate7(expirationDate.minusDays(7));
        alarm.setAlarmDate3(expirationDate.minusDays(3));
        alarm.setAlarmDate0(expirationDate);

        // 유통기한이 바뀌면 새로운 일정에 맞춰 다시 알림을 보낼 수 있도록 초기화
        alarm.setIsSent7(false);
        alarm.setIsSent3(false);
        alarm.setIsSent0(false);
    }

    @Transactional
    public void deleteIngredientAlarm(int ingredientId) {
        if (alarmRepository.existsByIngredientId(ingredientId)) {
            alarmRepository.deleteByIngredientId(ingredientId);
        }
    }

    // 보고서의 통합 테스트와 동일하게 매 분 실행.
    // 운영 환경에서는 예: 매일 오전 8시 -> 0 0 8 * * *
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkExpiringIngredients() {
        List<Alarm> alarms = alarmRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Alarm alarm : alarms) {
            User user = userRepository.findById(alarm.getUserId()).orElse(null);
            if (user == null || user.getEmail() == null) {
                continue;
            }

            if (!Boolean.TRUE.equals(user.getEmailNotify())) {
                continue;
            }

            Ingredient ingredient = ingredientRepository.findById(alarm.getIngredientId()).orElse(null);
            if (ingredient == null) {
                continue;
            }

            String email = user.getEmail();
            String ingredientName = ingredient.getName();
            String fridgeName = ingredient.getFridge().getFridgeName();
            LocalDate expirationDate = alarm.getAlarmDate0();

            if (today.equals(alarm.getAlarmDate7()) && !Boolean.TRUE.equals(alarm.getIsSent7())) {
                sendExpirationMail(email, ingredientName, fridgeName, expirationDate, 7);
                alarm.setIsSent7(true);
            }

            if (today.equals(alarm.getAlarmDate3()) && !Boolean.TRUE.equals(alarm.getIsSent3())) {
                sendExpirationMail(email, ingredientName, fridgeName, expirationDate, 3);
                alarm.setIsSent3(true);
            }

            if (today.equals(alarm.getAlarmDate0()) && !Boolean.TRUE.equals(alarm.getIsSent0())) {
                sendExpirationMail(email, ingredientName, fridgeName, expirationDate, 0);
                alarm.setIsSent0(true);
            }
        }
    }

    private void sendExpirationMail(String email,
                                    String ingredientName,
                                    String fridgeName,
                                    LocalDate expirationDate,
                                    int daysLeft) {
        String timing = daysLeft == 0 ? "당일" : daysLeft + "일 전";
        String subject = "[냉장고 알리미] '" + ingredientName + "' 유통기한 " + timing + " 알림";

        String html =
                "<div style='font-family:Arial,sans-serif;font-size:16px;'>"
                        + "<p style='font-size:30px;font-weight:bold;color:#3498db;'>❄️ 냉장고 재료 알리미</p>"
                        + "<p style='font-size:20px;'><b>" + ingredientName + " 유통기한 " + timing + "!</b></p>"
                        + "<p><b>냉장고 :</b> " + fridgeName + "</p>"
                        + "<p><b>재료 :</b> " + ingredientName + "</p>"
                        + "<p><b>유통기한 :</b> " + expirationDate + "</p>"
                        + "<p><b>남은 기간 :</b> " + (daysLeft == 0 ? "오늘" : daysLeft + "일") + "</p>"
                        + "<p>유통기한을 확인하고 빠른 시일 내에 섭취해 주세요.</p>"
                        + "</div>";

        emailService.sendHtmlEmail(email, subject, html);
    }
}
