package com.example.habitwarden_backend.domain;

import com.example.habitwarden_backend.domain.reqrespBody.RegisterRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Document("user")
public class User implements UserDetails {

    @Id
    ObjectId id;
    String name;
    String password;
    Integer age;
    Gender gender;
    List<Role> roles;
    String profession;
    String codeword;
    Boolean isDarkGroup;
    Integer points;

    public User(ObjectId id, String name, List<Role> roles) {
        this.id = id;
        this.name = name;
        this.roles = roles;
    }

    public static User ofRegisterRequest(RegisterRequest registeredUser) {
        Gender gender = switch (registeredUser.getGender().toUpperCase()) {
            case "M" -> Gender.M;
            case "W" -> Gender.W;
            default -> Gender.D;
        };

        return new User(
                null,
                registeredUser.getName(),
                registeredUser.getPassword(),
                Integer.valueOf(registeredUser.getAge()),
                gender,
                Collections.singletonList(Role.USER),
                registeredUser.getProfession(),
                registeredUser.getCodeword(),
                registeredUser.getIsDarkGroup(),
                0
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles;
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
