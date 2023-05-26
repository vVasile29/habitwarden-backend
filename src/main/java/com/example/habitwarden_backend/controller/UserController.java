package com.example.habitwarden_backend.controller;

import com.example.habitwarden_backend.domain.User;
import com.example.habitwarden_backend.service.JWTService;
import com.example.habitwarden_backend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final JWTService jwtService;
    private final UserService userService;

    @GetMapping("/currentUser")
    public Mono<User> getCurrentUser(ServerHttpRequest request) {
        String jwt = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // TODO change to optional
        if (jwt == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT"));
        }

        Mono<User> user = userService.findByUsername(jwtService.getUserName(jwt.substring(7)));
        return user.flatMap(u -> {
            if (u.getName() == null) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            }

            User myUser = new User(null, u.getName(), null);
            return Mono.just(myUser);
        });
    }

    @GetMapping("/getAllUsers")
    public Flux<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
