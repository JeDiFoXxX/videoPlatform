package ru.videoplatform.signaling.model;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomStatusTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("getValue() возвращает значение в нижнем регистре")
    void shouldReturnCorrectWireValue() {
        assertThat(RoomStatus.CALLING.getValue()).isEqualTo("calling");
        assertThat(RoomStatus.ACTIVE.getValue()).isEqualTo("active");
    }

    @Test
    @DisplayName("fromValue() корректно парсит валидные строки")
    void shouldParseValidValues() {
        assertThat(RoomStatus.fromValue("active")).isEqualTo(RoomStatus.ACTIVE);
        assertThat(RoomStatus.fromValue("ended")).isEqualTo(RoomStatus.ENDED);
    }

    @Test
    @DisplayName("fromValue() выбрасывает IllegalArgumentException на неизвестный статус")
    void shouldThrowExceptionOnUnknownValue() {
        assertThatThrownBy(() -> RoomStatus.fromValue("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный статус: unknown");
    }

    @Test
    @DisplayName("Cериализация enum в строку нижнего регистра")
    void shouldSerializeToJsonString() throws Exception {
        String json = objectMapper.writeValueAsString(RoomStatus.CALLING);
        assertThat(json).isEqualTo("\"calling\"");
    }

    @Test
    @DisplayName("Десериализация строки в валидный enum")
    void shouldDeserializeFromJsonString() throws Exception {
        RoomStatus status = objectMapper.readValue("\"active\"", RoomStatus.class);
        assertThat(status).isEqualTo(RoomStatus.ACTIVE);
    }
}