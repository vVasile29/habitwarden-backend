package com.example.habitwarden_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HabitDoneDataInfo {

    LocalDateTime doneTime;
    Boolean done;
    Boolean lieOnDone;

}
