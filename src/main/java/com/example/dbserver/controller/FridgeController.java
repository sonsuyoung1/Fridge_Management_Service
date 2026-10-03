package com.example.dbserver.controller;

import com.example.dbserver.entity.Fridge;
import com.example.dbserver.entity.User;
import com.example.dbserver.service.FridgeService;
import com.example.dbserver.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class FridgeController {

    private final FridgeService fridgeService;
    private final UserService userService;

    @GetMapping("/refrigerator")
    public String refrigerator(HttpSession session, Model model) {
        User user = requireUser(session);
        model.addAttribute("user", user);
        model.addAttribute("fridgeList", fridgeService.getFridges(user));
        return "refrigerator";
    }

    @GetMapping("/add-fridge")
    public String addFridgePage() {
        return "add_fridge";
    }

    @PostMapping("/add-fridge")
    public String addFridge(@RequestParam String fridgeName,
                            @RequestParam String fridgeType,
                            HttpSession session) {
        User user = requireUser(session);
        fridgeService.addFridge(user, fridgeName, fridgeType);
        return "redirect:/refrigerator?saved=true";
    }

    @GetMapping("/fridge/edit/{id}")
    public String editFridgePage(@PathVariable int id,
                                 HttpSession session,
                                 Model model) {
        User user = requireUser(session);
        Fridge fridge = fridgeService.findById(id, user);
        model.addAttribute("fridge", fridge);
        return "edit_fridge";
    }

    @PostMapping("/fridge/edit/{id}")
    public String editFridge(@PathVariable int id,
                             @RequestParam String fridgeName,
                             @RequestParam String fridgeType,
                             HttpSession session) {
        User user = requireUser(session);
        fridgeService.updateFridge(id, user, fridgeName, fridgeType);
        return "redirect:/refrigerator?saved=true";
    }

    @PostMapping("/fridge/delete/{id}")
    public String deleteFridge(@PathVariable int id, HttpSession session) {
        User user = requireUser(session);
        fridgeService.deleteFridge(id, user);
        return "redirect:/refrigerator";
    }

    private User requireUser(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return userService.findById(userId);
    }
}
