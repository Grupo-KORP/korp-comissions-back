package com.comissions.korp.config.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String[] ROUTE_PERMIT = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/swagger-ui.html"
    };

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Não autorizado!")
                        )
                )
                .cors(Customizer.withDefaults())
                .csrf(CsrfConfigurer::spa)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuario").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuario/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuario/**").hasRole("FINAN")
                        .requestMatchers(HttpMethod.GET, "/usuario/**").hasRole("ADMIN")
                        .requestMatchers(ROUTE_PERMIT).permitAll()
                        .requestMatchers("/financeiro/**").hasAnyRole("FINAN", "ADMIN")
                        .requestMatchers("/api/pedidos/rascunhos", "/api/pedidos/rascunhos/**").hasRole("VEND")

                        // ── Área do vendedor ─────────────────────────────────────────
                        // A ordem importa: a primeira regra que casar com a URL é a aplicada.
                        // Histórico de pedidos de um vendedor: também usado pela tela de vendedores do financeiro.
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/vendedor/**").hasAnyRole("VEND", "FINAN", "ADMIN")
                        // Consultas avulsas de pedido: exigem login (o dono do pedido é conferido no service).
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/listar", "/api/pedidos/buscar/**").authenticated()
                        // Criar, editar, finalizar e excluir pedidos e itens: só o vendedor.
                        .requestMatchers("/api/pedidos/**", "/api/item_pedido/**").hasRole("VEND")
                        .requestMatchers("/api/vendedor/**").hasRole("VEND")
                        // Cadastros usados pelo vendedor: exigem login.
                        .requestMatchers("/api/cliente/**", "/api/distribuidor/**", "/api/contatos/**", "/api/produto/**").authenticated()

                        // Descomente para proteger por role:
                        // .requestMatchers("/financeiro/**").hasRole("FINAN")
                        .anyRequest().permitAll()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.setContentType("application/json");
            String json = """
                    {
                      "timestamp":"%s",
                      "status":403,
                      "error":"Forbidden",
                      "message":"Você não tem permissão para acessar este recurso.",
                      "path":"%s"
                    }
                    """.formatted(java.time.Instant.now(), request.getRequestURI());
            response.getWriter().write(json);
        };
    }
}
