package com.example.habitwarden_backend.domain;

import lombok.Data;

@Data
public class HabitDoneDataRequest {

    String userName;
    String habitName;
    String date;

}