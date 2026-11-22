package com.dimas.minidoodle.service;

import com.dimas.minidoodle.domain.User;
import com.dimas.minidoodle.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public User create(String name, String email) {
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A user with email '" + email + "' already exists");
        }
        return users.save(new User(name, email.toLowerCase()));
    }

    @Transactional(readOnly = true)
    public User get(UUID id) {
        return users.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }
}