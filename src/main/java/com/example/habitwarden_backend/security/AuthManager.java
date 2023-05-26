package com.example.habitwarden_backend.security;

import com.example.habitwarden_backend.domain.User;
import com.example.habitwarden_backend.service.JWTService;
import com.example.habitwarden_backend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class AuthManager implements ReactiveAuthenticationManager {
    private final JWTService jwtService;
    private final UserService userService;

    // gets the token from AuthConverter and tests if it is valid
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .cast(BearerToken.class)
                .flatMap(auth -> {
                    String userName = jwtService.getUserName(auth.getCredentials());
                    if (userName == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalid"));
                    }
                    Mono<User> foundUser = userService.findByUsername(userName);
                    return foundUser.flatMap(u -> {
                        if (jwtService.validate(u, auth.getCredentials())) {
                            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(u.getName(), u.getPassword(), u.getAuthorities());
                            return Mono.just(token);
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalid"));
                    });
                });
    }
}
