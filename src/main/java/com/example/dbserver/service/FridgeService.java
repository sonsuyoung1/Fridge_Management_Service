package com.example.dbserver.service;

import com.example.dbserver.entity.Fridge;
import com.example.dbserver.entity.User;
import com.example.dbserver.repository.FridgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FridgeService {

    private final FridgeRepository fridgeRepository;

    public List<Fridge> getFridges(User user) {
        return fridgeRepository.findByUserOrderByFridgeIdAsc(user);
    }

    public Fridge findById(int fridgeId, User user) {
        return fridgeRepository.findById(fridgeId)
                .filter(fridge -> fridge.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("냉장고를 찾을 수 없습니다."));
    }

    @Transactional
    public Fridge addFridge(User user, String fridgeName, String fridgeType) {
        Fridge fridge = new Fridge();
        fridge.setUser(user);
        fridge.setFridgeName(fridgeName);
        fridge.setFridgeType(fridgeType);
        fridge.setRegDate(LocalDateTime.now());
        return fridgeRepository.save(fridge);
    }

    @Transactional
    public void updateFridge(int fridgeId, User user, String fridgeName, String fridgeType) {
        Fridge fridge = findById(fridgeId, user);
        fridge.setFridgeName(fridgeName);
        fridge.setFridgeType(fridgeType);
        fridge.setUpdated(LocalDateTime.now());
    }

    @Transactional
    public void deleteFridge(int fridgeId, User user) {
        Fridge fridge = findById(fridgeId, user);
        fridgeRepository.delete(fridge);
    }
}
