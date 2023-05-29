package com.example.habitwarden_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserHabitData {

    String userName;
    List<HabitDoneData> habitDoneData;

}
