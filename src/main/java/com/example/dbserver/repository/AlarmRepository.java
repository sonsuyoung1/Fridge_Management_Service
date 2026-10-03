package com.example.dbserver.repository;

import com.example.dbserver.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    boolean existsByIngredientId(Integer ingredientId);

    Alarm findByIngredientId(Integer ingredientId);

    void deleteByIngredientId(Integer ingredientId);
}
