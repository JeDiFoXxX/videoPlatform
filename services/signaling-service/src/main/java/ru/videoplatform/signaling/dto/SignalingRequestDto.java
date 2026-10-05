package ru.videoplatform.signaling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SignalingRequestDto(
        @JsonProperty("event") String event,
        @JsonProperty("teacher_id") String teacherId,
        @JsonProperty("student_id") String studentId,
        @JsonProperty("payload") Object payload
) { }
