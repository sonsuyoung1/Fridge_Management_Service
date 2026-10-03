package com.example.dbserver.controller;

import com.example.dbserver.entity.User;
import com.example.dbserver.service.EmailService;
import com.example.dbserver.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = userService.login(username, password);
            session.setAttribute("userId", user.getUserId());
            return "redirect:/refrigerator";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirm,
                           @RequestParam String name,
                           @RequestParam String email,
                           Model model) {
        if (!password.equals(confirm)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "register";
        }

        try {
            userService.register(username, password, name, email);
            return "register_success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/check-username")
    @ResponseBody
    public String checkUsername(@RequestParam String username) {
        return userService.isUsernameDuplicate(username) ? "duplicate" : "ok";
    }

    @GetMapping("/check-email")
    @ResponseBody
    public String checkEmail(@RequestParam String email) {
        return userService.isEmailDuplicate(email) ? "duplicate" : "ok";
    }

    @GetMapping("/find/id")
    public String findIdPage() {
        return "findid";
    }

    @PostMapping("/find/id")
    public String findId(@RequestParam String email, Model model) {
        userService.findByEmail(email)
                .ifPresentOrElse(
                        user -> model.addAttribute("result", "아이디: " + user.getUsername()),
                        () -> model.addAttribute("error", "해당 이메일로 가입된 계정이 없습니다.")
                );
        return "findid";
    }

    @GetMapping("/find/password")
    public String findPasswordPage() {
        return "findpassword";
    }

    @PostMapping("/find/password")
    public String findPassword(@RequestParam String username,
                               @RequestParam String email,
                               Model model) {
        userService.findByUsernameAndEmail(username, email)
                .ifPresentOrElse(user -> {
                    emailService.sendTextEmail(
                            user.getEmail(),
                            "[냉장고 알리미] 비밀번호 안내",
                            "현재 비밀번호는 " + user.getPassword() + " 입니다."
                    );
                    model.addAttribute("result", "가입한 이메일로 비밀번호 안내를 전송했습니다.");
                }, () -> model.addAttribute("error", "입력한 계정 정보를 확인해주세요."));
        return "findpassword";
    }

    @GetMapping("/user/settings")
    public String settings(HttpSession session, Model model) {
        User user = requireUser(session);
        model.addAttribute("user", user);
        return "user_settings";
    }

    @GetMapping("/user/my-info")
    public String myInfo(HttpSession session, Model model) {
        model.addAttribute("user", requireUser(session));
        return "my_info";
    }

    @PostMapping("/user/email-notify")
    public String updateEmailNotify(@RequestParam(defaultValue = "false") boolean emailNotify,
                                    HttpSession session) {
        User user = requireUser(session);
        userService.updateEmailNotify(user.getUserId(), emailNotify);
        return "redirect:/user/settings?notify_saved=true";
    }

    @GetMapping("/user/change-email")
    public String changeEmailPage() {
        return "change_email";
    }

    @PostMapping("/user/change-email")
    public String changeEmail(@RequestParam String currentPassword,
                              @RequestParam String newEmail,
                              HttpSession session,
                              Model model) {
        User user = requireUser(session);
        try {
            userService.updateEmail(user.getUserId(), currentPassword, newEmail);
            return "redirect:/user/settings?email_success=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "change_email";
        }
    }

    @GetMapping("/user/change-password")
    public String changePasswordPage() {
        return "change_password";
    }

    @PostMapping("/user/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "새 비밀번호가 일치하지 않습니다.");
            return "change_password";
        }

        User user = requireUser(session);
        try {
            userService.updatePassword(user.getUserId(), currentPassword, newPassword);
            return "redirect:/user/settings?pw_success=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "change_password";
        }
    }

    private User requireUser(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return userService.findById(userId);
    }
}
