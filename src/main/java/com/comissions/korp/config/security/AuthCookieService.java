package com.comissions.korp.config.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class AuthCookieService {

    public static final String AUTH_COOKIE_NAME = "KORP_AUTH";

    @Value("${jwt.expiration}")
    private long expirationMillis;

    @Value("${auth.cookie.secure:false}")
    private boolean secure;

    @Value("${auth.cookie.same-site:Lax}")
    private String sameSite;

    public Optional<String> readToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> AUTH_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .filter(value -> !value.isBlank());
    }

    public void writeToken(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie", buildCookie(token, expirationMillis).toString());
    }

    public void clearToken(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookie("", 0).toString());
    }

    public void clearCsrfToken(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCsrfCookie("", 0).toString());
    }

    private ResponseCookie buildCookie(String value, long maxAgeMillis) {
        return ResponseCookie.from(AUTH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofMillis(maxAgeMillis))
                .build();
    }

    private ResponseCookie buildCsrfCookie(String value, long maxAgeMillis) {
        return ResponseCookie.from("XSRF-TOKEN", value)
                .httpOnly(false)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofMillis(maxAgeMillis))
                .build();
    }
}
