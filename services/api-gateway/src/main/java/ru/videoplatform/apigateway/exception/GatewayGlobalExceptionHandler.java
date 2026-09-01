package ru.videoplatform.apigateway.exception;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@NullMarked
@Order(-2)
@RequiredArgsConstructor
public class GatewayGlobalExceptionHandler implements WebExceptionHandler {

    private final AntPathMatcher pathMatcher;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        var path = exchange.getRequest().getPath().value();
        if (pathMatcher.match("/**/event", path)) {
            var response = exchange.getResponse();
            if (response.isCommitted()) {
                return Mono.error(exception);
            }
            response.setStatusCode(HttpStatus.OK);
            return response.setComplete();
        }
        return Mono.error(exception);
    }
}
