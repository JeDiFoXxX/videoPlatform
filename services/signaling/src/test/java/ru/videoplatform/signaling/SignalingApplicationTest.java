package ru.videoplatform.signaling;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SignalingApplicationTest {

    @Test
    @DisplayName("Spring Boot должен успешно запускаться")
    void contextLoads() { }
}