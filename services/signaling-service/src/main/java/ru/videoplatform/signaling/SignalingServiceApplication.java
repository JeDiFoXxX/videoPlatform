package ru.videoplatform.signaling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SignalingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignalingServiceApplication.class, args);
    }

}
