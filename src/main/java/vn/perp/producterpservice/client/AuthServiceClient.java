package vn.perp.producterpservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.perp.producterpservice.config.FeignConfig;

import java.util.UUID;

@FeignClient(
        name = "auth-service",
        url = "${AUTH_SERVICE_URL:http://localhost:8081}",
        configuration = FeignConfig.class
)
public interface AuthServiceClient {

    @GetMapping("/api/internal/users/{userId}/exists")
    Boolean userExists(@PathVariable("userId") UUID userId);
}
