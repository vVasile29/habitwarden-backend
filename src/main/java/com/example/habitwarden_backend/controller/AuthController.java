package com.example.habitwarden_backend.controller;

import com.example.habitwarden_backend.domain.User;
import com.example.habitwarden_backend.domain.reqrespBody.LoginRequest;
import com.example.habitwarden_backend.domain.reqrespBody.RegisterRequest;
import com.example.habitwarden_backend.domain.reqrespModel.ReqRespModel;
import com.example.habitwarden_backend.service.JWTService;
import com.example.habitwarden_backend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JWTService jwtService;
    private final PasswordEncoder encoder;
    private final UserService userService;

    @GetMapping("/test")
    public Mono<ResponseEntity<ReqRespModel<String>>> auth() {
        return Mono.just(ResponseEntity.ok(new ReqRespModel<>("Welcome!", "welcome message")));
    }

    // in web dev we would add ServerHttpRequest request, ServerHttpResponse response,
    // and then also do addJWTHttpOnlyCookie(request, response, jwt);
    @PostMapping("/login")
    public Mono<ResponseEntity<ReqRespModel<String>>> login(@RequestBody LoginRequest user) {
        // TODO change email to name because has to be anonymous
        Mono<User> foundUser = userService.findByUsername(user.getName());
        return foundUser.flatMap(u -> {
            if (u.getUsername() == null) {
                System.out.println("no user found!");
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            }
            // here happens password matching from login input to database bcrypt password
            if (encoder.matches(user.getPassword(), u.getPassword())) {
                String jwt = jwtService.generate(u.getUsername());
                return Mono.just(ResponseEntity.ok(new ReqRespModel<>(jwt, "Token was successfully created")));
            }
            System.out.println("invalid credentials");
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials"));
        });
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<ReqRespModel<String>>> register(@RequestBody RegisterRequest user) {
        Mono<Boolean> userAlreadyExists = userService.existsByName(user.getName());

        return userAlreadyExists.flatMap(exists -> {
            if (exists) {
                return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "User already exists!"));
            } else {
                Mono<User> registeredUser = userService.saveNewUser(user);
                return registeredUser.flatMap(u -> {
                    if (u.getName() == null) {
                        System.out.println("Something went wrong while saving the user!");
                        return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while saving the user!"));
                    }
                    System.out.println("User " + u.getName() + " was successfully registered!");
                    return Mono.just(ResponseEntity.ok(new ReqRespModel<>(u.getName(), "User " + u.getName() + " was successfully registered!")));
                });
            }
        });
    }

    private static void addJWTHttpOnlyCookie(ServerHttpRequest request, ServerHttpResponse response, String jwt) {
        response.addCookie(ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .path("/")
                .secure(request.getURI().getScheme().equals("https"))
                .maxAge(Duration.ofHours(1))
                .build());
    }

//    @PostMapping("/logout")
//    public Mono<ResponseEntity<ReqRespModel<String>>> logout(ServerHttpRequest request, ServerHttpResponse response) {
//        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
//        if (cookies.containsKey("jwt")) {
//            ResponseCookie deletedCookie = ResponseCookie.from("jwt", "")
//                    .path("/")
//                    .maxAge(0)
//                    .httpOnly(true)
//                    .build();
//            response.addCookie(deletedCookie);
//        }
//        return Mono.empty();
//    }

}
