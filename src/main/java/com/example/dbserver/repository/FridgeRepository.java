package com.example.dbserver.repository;

import com.example.dbserver.entity.Fridge;
import com.example.dbserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FridgeRepository extends JpaRepository<Fridge, Integer> {

    List<Fridge> findByUserOrderByFridgeIdAsc(User user);
}
