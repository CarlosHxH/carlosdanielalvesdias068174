package com.album.seplag.config;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> rateLimitBuckets;
    private final RateLimitConfig rateLimitConfig;

    public RateLimitInterceptor(Map<String, Bucket> rateLimitBuckets, RateLimitConfig rateLimitConfig) {
        this.rateLimitBuckets = rateLimitBuckets;
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }

        String username = authentication.getName();
        Bucket bucket = rateLimitBuckets.computeIfAbsent(username, k -> rateLimitConfig.createBucket());
        
        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            try {
                int limit = rateLimitConfig.getRequestsPerMinute();
                response.getWriter().write("{\"message\":\"Rate limit exceeded. Maximum " + limit + " requests per minute.\"}");
            } catch (IOException e) {
                log.error("Error writing rate limit response", e);
                return false;
            }
            return false;
        }
    }
}