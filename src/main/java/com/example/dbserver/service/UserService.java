package com.example.dbserver.service;

import com.example.dbserver.entity.User;
import com.example.dbserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean isUsernameDuplicate(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User register(String username, String password, String name, String email) {
        if (isUsernameDuplicate(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (isEmailDuplicate(email)) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setName(name);
        user.setEmail(email);
        user.setEmailNotify(true);
        user.setRegDate(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        user.setLastLogin(LocalDateTime.now());
        return user;
    }

    public User findById(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsernameAndEmail(String username, String email) {
        return userRepository.findByUsernameAndEmail(username, email);
    }

    @Transactional
    public void updateEmail(int userId, String currentPassword, String newEmail) {
        User user = findById(userId);
        if (!user.getPassword().equals(currentPassword)) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        user.setEmail(newEmail);
    }

    @Transactional
    public void updatePassword(int userId, String currentPassword, String newPassword) {
        User user = findById(userId);
        if (!user.getPassword().equals(currentPassword)) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        user.setPassword(newPassword);
    }

    @Transactional
    public void updateEmailNotify(int userId, boolean enabled) {
        User user = findById(userId);
        user.setEmailNotify(enabled);
    }
}
