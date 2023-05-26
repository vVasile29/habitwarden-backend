package com.example.habitwarden_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Document("dateData")
public class DateData {

    ObjectId id;
    User user;
    LocalDate date;
    List<HabitDoneData> habitDoneData;

}
