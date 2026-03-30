package com.smartclassroom.service;

import com.smartclassroom.entity.User;
import com.smartclassroom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * UserService - handles user registration, retrieval, and management.
 */
@Service
@Transactional
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /** Register a new user - encodes password before saving */
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already taken: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }
        // Hash password before persisting
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAllStudents() {
        return userRepository.findByRole(User.Role.STUDENT);
    }

    public List<User> findAllFaculty() {
        return userRepository.findByRole(User.Role.FACULTY);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    /** Admin: toggle a user's enabled/disabled status */
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    /** Dashboard stats for admin */
    public long countStudents()  { return userRepository.countByRole(User.Role.STUDENT); }
    public long countFaculty()   { return userRepository.countByRole(User.Role.FACULTY); }
    public long countAll()       { return userRepository.count(); }
}