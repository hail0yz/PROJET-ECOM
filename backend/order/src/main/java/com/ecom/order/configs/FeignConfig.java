package com.ecom.order.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import feign.Response;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignAuthInterceptor();
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    private static class FeignAuthInterceptor implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate template) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String token = jwtAuth.getToken().getTokenValue();
                template.header("Authorization", "Bearer " + token);
            }
        }
    }

    private static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, Response response) {
            int status = response.status();

            log.error("Feign client error - Method: {}, Status: {}, Reason: {}", 
                    methodKey, status, response.reason());

            switch (status) {
                case 400:
                    return new FeignBadRequestException("Bad request: " + response.reason());
                case 401:
                    return new FeignUnauthorizedException("Unauthorized: " + response.reason());
                case 403:
                    return new FeignForbiddenException("Forbidden: " + response.reason());
                case 404:
                    return new FeignNotFoundException("Resource not found: " + response.reason());
                case 500:
                    return new FeignServerException("Internal server error: " + response.reason());
                case 503:
                    return new FeignServiceUnavailableException("Service unavailable: " + response.reason());
                default:
                    return defaultErrorDecoder.decode(methodKey, response);
            }
        }
    }

    // Custom Exceptions
    public static class FeignBadRequestException extends FeignException {
        public FeignBadRequestException(String message) {
            super(400, message);
        }
    }

    public static class FeignUnauthorizedException extends FeignException {
        public FeignUnauthorizedException(String message) {
            super(401, message);
        }
    }

    public static class FeignForbiddenException extends FeignException {
        public FeignForbiddenException(String message) {
            super(403, message);
        }
    }

    public static class FeignNotFoundException extends FeignException {
        public FeignNotFoundException(String message) {
            super(404, message);
        }
    }

    public static class FeignServerException extends FeignException {
        public FeignServerException(String message) {
            super(500, message);
        }
    }

    public static class FeignServiceUnavailableException extends FeignException {
        public FeignServiceUnavailableException(String message) {
            super(503, message);
        }
    }

}
