package ru.videoplatform.signaling.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandshakeInterceptor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request.getPrincipal() instanceof JwtAuthenticationToken jwt) {
            var userId = jwt.getToken().getSubject();
            var tokenData = objectMapper.convertValue(jwt.getTokenAttributes(), TokenAttribute.class);
            var role = tokenData.realmAccess().roles().stream()
                    .filter(r -> "ROLE_TEACHER".equals(r) || "ROLE_STUDENT".equals(r))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
            attributes.put("userId", userId);
            attributes.put("userRole", role);
            return true;
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) { }

    private record TokenAttribute(@JsonProperty("realm_access") RealmAccess realmAccess) { }

    private record RealmAccess(@JsonProperty("roles") List<String> roles) { }
}
