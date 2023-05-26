package com.example.habitwarden_backend.service;

import com.example.habitwarden_backend.domain.User;
import com.example.habitwarden_backend.domain.reqrespBody.RegisterRequest;
import com.example.habitwarden_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> findByUsername(String userName) {
        return userRepository.findByName(userName);
    }

    public Mono<User> saveNewUser(RegisterRequest user) {
        User newUser = User.ofRegisterRequest(user);
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(newUser);
    }

    public Mono<User> getUserByName(String name) {
        return userRepository.findByName(name);
    }

    public Mono<Boolean> existsByName(String name) {
        return userRepository.existsByName(name);
    }

}
