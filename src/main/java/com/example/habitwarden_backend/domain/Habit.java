package com.example.habitwarden_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Document("habit")
public class Habit {

    @Id
    ObjectId id;
    String name;
    Integer pointsPerTask;
    Integer pointsPerDay;
    Integer amountPerTask;
    Integer timesPerDay;
    Double fakeUserCancellationRate;
    NotificationText notificationText;

}
