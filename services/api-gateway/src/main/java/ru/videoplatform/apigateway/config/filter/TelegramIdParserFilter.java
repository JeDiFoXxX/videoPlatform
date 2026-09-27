package ru.videoplatform.apigateway.config.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class TelegramIdParserFilter {

    private final ObjectMapper objectMapper;

    public GatewayFilter apply() {
        return (exchange, chain) -> Mono.fromCallable(() -> {
                    parseAndSaveId(exchange);
                    return exchange;
                })
                .onErrorMap(error -> new RuntimeException())
                .flatMap(chain::filter);
    }

    @SneakyThrows
    private void parseAndSaveId(ServerWebExchange exchange) {
        var cachedBody = exchange.getAttribute("cachedRequestBody");

        if (!(cachedBody instanceof String jsonString)) {
            throw new Exception();
        }

        var update = objectMapper.readValue(jsonString, TelegramUpdate.class);
        var extractedTgId = 0L;

        if (update.message() != null && update.message().from() != null) {
            extractedTgId = update.message().from().id();
        }

        if (update.callbackQuery() != null && update.callbackQuery().from() != null) {
            extractedTgId = update.callbackQuery().from().id();
        }

        if (extractedTgId == 0L) {
            throw new Exception();
        }

        exchange.getAttributes().put("extractedTgId", String.valueOf(extractedTgId));
    }

    private record TelegramUpdate(
            @JsonProperty("message") TelegramMessage message,
            @JsonProperty("callback_query") TelegramCallbackQuery callbackQuery
    ) {
    }

    private record TelegramMessage(@JsonProperty("from") TelegramUser from) {
    }

    private record TelegramCallbackQuery(@JsonProperty("from") TelegramUser from) {
    }

    private record TelegramUser(@JsonProperty("id") Long id) {
    }
}
