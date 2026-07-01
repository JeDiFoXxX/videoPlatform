package ru.videoplatform.signaling.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoomStatus {
    CALLING("calling"),
    ACTIVE("active"),
    REJECTED("rejected"),
    ENDED("ended");

    private final String value;

    RoomStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RoomStatus fromValue(String value) {
        for (RoomStatus status : RoomStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неверный статус: " + value);
    }
}
