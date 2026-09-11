package com.comissions.korp.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.JwtException;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private AuthCookieService authCookieService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = authCookieService.readToken(request).orElse(null);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtService.extrairEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = usuarioDetailsService.loadUserByUsername(email);
                if (!jwtService.tokenValido(token, userDetails)) {
                    authCookieService.clearToken(response);
                    if (isAuthBootstrapRequest(request)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessão inválida ou expirada");
                        return;
                    }
                } else {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, token, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {
            authCookieService.clearToken(response);
            if (isAuthBootstrapRequest(request)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessão inválida ou expirada");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthBootstrapRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.endsWith("/auth/login")
                && !uri.endsWith("/auth/csrf")
                && !uri.endsWith("/auth/logout");
    }
}
