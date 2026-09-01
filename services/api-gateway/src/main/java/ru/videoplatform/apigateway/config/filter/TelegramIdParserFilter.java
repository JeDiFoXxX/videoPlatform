package ru.videoplatform.apigateway.config.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class TelegramIdParserFilter {

    private final ObjectMapper objectMapper;

    public GatewayFilter apply() {
        return (exchange, chain) -> Mono.fromRunnable(() -> parseAndSaveId(exchange))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.defer(() -> chain.filter(exchange)));
    }

    @SneakyThrows
    private void parseAndSaveId(ServerWebExchange exchange) {
        var cachedBody = exchange.getAttribute("cachedRequestBody");
        if (!(cachedBody instanceof String jsonString)) {
            throw new RuntimeException("cachedRequestBody is missing or is not a String");
        }

        var update = objectMapper.readValue(jsonString, TelegramUpdate.class);
        if (update.message() == null || update.message().from() == null || update.message().from().id() == null) {
            throw new RuntimeException("Telegram User ID not found in the JSON structure");
        }

        var extractedTgId = update.message().from().id().toString();
        exchange.getAttributes().put("extractedTgId", extractedTgId);
    }

    private record TelegramUpdate(@JsonProperty("message") TelegramMessage message) {
        private record TelegramMessage(@JsonProperty("from") TelegramUser from) {
            private record TelegramUser(@JsonProperty("id") Long id) {
            }
        }
    }
}
