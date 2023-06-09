package com.example.habitwarden_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HabitDoneData {

    String habitName;
    List<HabitDoneDataInfo> habitDoneDataInfo;

}
