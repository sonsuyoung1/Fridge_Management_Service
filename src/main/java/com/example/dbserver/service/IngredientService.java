package com.example.dbserver.service;

import com.example.dbserver.entity.Fridge;
import com.example.dbserver.entity.Ingredient;
import com.example.dbserver.entity.User;
import com.example.dbserver.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final AlarmService alarmService;

    @Transactional
    public void addIngredient(User user, Fridge fridge, String name, String storageType,
                              String category, int quantity, LocalDate expirationDate) {
        Ingredient ingredient = new Ingredient();
        ingredient.setUser(user);
        ingredient.setFridge(fridge);
        ingredient.setName(name);
        ingredient.setStorageType(storageType);
        ingredient.setCategory(category);
        ingredient.setQuantity(quantity);
        ingredient.setExpirationDate(expirationDate);
        ingredient.setRegDate(LocalDateTime.now());

        Ingredient saved = ingredientRepository.save(ingredient);
        alarmService.createIngredientAlarms(saved);
    }

    public Ingredient findById(int id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("재료를 찾을 수 없습니다."));
    }

    public List<Ingredient> getIngredients(Fridge fridge) {
        return ingredientRepository.findByFridgeOrderByIdAsc(fridge);
    }

    @Transactional
    public void updateIngredient(int id, User user, String name, String storageType,
                                 String category, int quantity, LocalDate expirationDate) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .filter(i -> i.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("수정 권한이 없습니다."));

        ingredient.setName(name);
        ingredient.setStorageType(storageType);
        ingredient.setCategory(category);
        ingredient.setQuantity(quantity);
        ingredient.setExpirationDate(expirationDate);
        ingredient.setUpdated(LocalDateTime.now());

        ingredientRepository.save(ingredient);
        alarmService.updateIngredientAlarms(ingredient);
    }

    @Transactional
    public void deleteIngredient(int id, User user) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .filter(i -> i.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("삭제 권한이 없습니다."));

        ingredientRepository.delete(ingredient);
    }
}
