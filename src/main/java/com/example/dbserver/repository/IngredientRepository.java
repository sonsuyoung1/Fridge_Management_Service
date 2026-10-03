package com.example.dbserver.repository;

import com.example.dbserver.entity.Fridge;
import com.example.dbserver.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {

    List<Ingredient> findByFridgeOrderByIdAsc(Fridge fridge);
}
