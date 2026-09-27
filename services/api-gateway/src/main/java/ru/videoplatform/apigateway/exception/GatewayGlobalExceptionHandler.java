package ru.videoplatform.apigateway.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
@NullMarked
@Order(-2)
@RequiredArgsConstructor
public class GatewayGlobalExceptionHandler implements WebExceptionHandler {

    private final AntPathMatcher pathMatcher;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        var path = exchange.getRequest().getPath().value();
        if (pathMatcher.match("/**/event", path)) {
            var response = exchange.getResponse();

            if (response.isCommitted()) {
                return Mono.error(exception);
            }

            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            if (exchange.getAttribute("cachedRequestBody") instanceof String cachedBody
            && exchange.getAttribute("extractedTgId") instanceof String chatId) {
                try {
                    var responseBytes = response.bufferFactory().wrap(
                            objectMapper.writeValueAsBytes(parseJsonWithSneakyThrows(cachedBody, chatId)));
                    return response.writeWith(Mono.just(responseBytes));
                } catch (Exception e) {
                    return response.setComplete();
                }
            }
            return response.setComplete();
        }
        return Mono.error(exception);
    }

    @SneakyThrows
    private Map<String, Object> parseJsonWithSneakyThrows(String cachedBody, String telegramId) {
        var telegramJson = new HashMap<String, Object>();
        telegramJson.put("method", "sendMessage");
        telegramJson.put("chat_id", telegramId);
        telegramJson.put("text", "⚠️ Произошла ошибка. Попробуйте позже.");

        if (cachedBody.contains("callback_query")) {
            var update = objectMapper.readValue(cachedBody, TelegramUpdate.class);
            var messageId = update.callbackQuery().message().messageId();
            telegramJson.put("method", "editMessageText");
            telegramJson.put("message_id", messageId);
        }
        return telegramJson;
    }

    private record TelegramUpdate(
            @JsonProperty("callback_query") TelegramCallbackQuery callbackQuery) { }

    private record TelegramCallbackQuery(
            @JsonProperty("message") TelegramMessage message) { }

    private record TelegramMessage(
            @JsonProperty("message_id") Long messageId) { }
}
