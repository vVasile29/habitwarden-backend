package com.example.habitwarden_backend.domain.reqrespBody;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {
    private String name;
    private String password;
}
