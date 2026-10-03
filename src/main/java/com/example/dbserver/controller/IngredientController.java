package com.example.dbserver.controller;

import com.example.dbserver.entity.Fridge;
import com.example.dbserver.entity.Ingredient;
import com.example.dbserver.entity.User;
import com.example.dbserver.service.FridgeService;
import com.example.dbserver.service.IngredientService;
import com.example.dbserver.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;
    private final FridgeService fridgeService;
    private final UserService userService;

    @GetMapping("/MainPage")
    public String mainPage(@RequestParam int fridgeId,
                           HttpSession session,
                           Model model) {
        User user = requireUser(session);
        Fridge fridge = fridgeService.findById(fridgeId, user);

        model.addAttribute("user", user);
        model.addAttribute("fridge", fridge);
        model.addAttribute("ingredientList", ingredientService.getIngredients(fridge));
        model.addAttribute("today", LocalDate.now());
        return "MainPage";
    }

    @GetMapping("/ingredient/add")
    public String addIngredientPage(@RequestParam int fridgeId,
                                    HttpSession session,
                                    Model model) {
        User user = requireUser(session);
        model.addAttribute("fridge", fridgeService.findById(fridgeId, user));
        return "add_ingredient";
    }

    @PostMapping("/ingredient/add")
    public String addIngredient(@RequestParam int fridgeId,
                                @RequestParam String name,
                                @RequestParam String storageType,
                                @RequestParam String category,
                                @RequestParam int quantity,
                                @RequestParam LocalDate expirationDate,
                                HttpSession session) {
        User user = requireUser(session);
        Fridge fridge = fridgeService.findById(fridgeId, user);

        ingredientService.addIngredient(
                user, fridge, name, storageType, category, quantity, expirationDate
        );
        return "redirect:/MainPage?fridgeId=" + fridgeId;
    }

    @GetMapping("/ingredient/edit/{id}")
    public String editIngredientPage(@PathVariable int id,
                                     HttpSession session,
                                     Model model) {
        User user = requireUser(session);
        Ingredient ingredient = ingredientService.findById(id);
        if (!ingredient.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        model.addAttribute("ingredient", ingredient);
        return "edit_ingredient";
    }

    @PostMapping("/ingredient/edit/{id}")
    public String editIngredient(@PathVariable int id,
                                 @RequestParam String name,
                                 @RequestParam String storageType,
                                 @RequestParam String category,
                                 @RequestParam int quantity,
                                 @RequestParam LocalDate expirationDate,
                                 HttpSession session) {
        User user = requireUser(session);
        Ingredient ingredient = ingredientService.findById(id);
        int fridgeId = ingredient.getFridge().getFridgeId();

        ingredientService.updateIngredient(
                id, user, name, storageType, category, quantity, expirationDate
        );
        return "redirect:/MainPage?fridgeId=" + fridgeId;
    }

    @PostMapping("/ingredient/delete/{id}")
    public String deleteIngredient(@PathVariable int id, HttpSession session) {
        User user = requireUser(session);
        Ingredient ingredient = ingredientService.findById(id);
        int fridgeId = ingredient.getFridge().getFridgeId();

        ingredientService.deleteIngredient(id, user);
        return "redirect:/MainPage?fridgeId=" + fridgeId;
    }

    private User requireUser(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return userService.findById(userId);
    }
}
