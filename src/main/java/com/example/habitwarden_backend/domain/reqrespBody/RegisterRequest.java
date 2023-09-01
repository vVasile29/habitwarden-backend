package com.example.habitwarden_backend.domain.reqrespBody;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest {
    private String name;
    private String password;
    private String age;
    private String gender;
    private String profession;
    private String codeword;
    private Boolean isDarkGroup;
}
