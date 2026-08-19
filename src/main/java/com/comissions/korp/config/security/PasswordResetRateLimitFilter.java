package com.comissions.korp.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PasswordResetRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public PasswordResetRateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        if (request.getRequestURI().equals("/usuario/esqueci-senha")
                && request.getMethod().equals("POST")) {

            String ip = extractIp(request);

            if (!rateLimitService.tryConsume(ip)) {
                response.setStatus(429);
                response.setHeader("Retry-After", "3600");
                response.getWriter().write("{\"message\":\"Muitas tentativas, tente novamente mais tarde.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String extractIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}