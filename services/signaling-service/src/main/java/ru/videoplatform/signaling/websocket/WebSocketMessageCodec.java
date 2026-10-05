package ru.videoplatform.signaling.websocket;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import ru.videoplatform.signaling.dto.SignalingRequestDto;
import ru.videoplatform.signaling.dto.SignalingResponseDto;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class WebSocketMessageCodec {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public TextMessage createMessage(String event, Object response) {
        var responseDto = new SignalingResponseDto(event, response);
        var json = objectMapper.writeValueAsString(responseDto);
        return new TextMessage(json);
    }

    public SignalingRequestDto receiveMessage(TextMessage message) throws Exception {
        return objectMapper.readValue(message.getPayload(), SignalingRequestDto.class);
    }
}
