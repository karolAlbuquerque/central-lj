package br.edu.central.centrallj.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import br.edu.central.centrallj.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Bean
  CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
    CorsConfiguration configuration = new CorsConfiguration();
    var patterns =
        corsProperties.allowedOriginPatterns() == null
                || corsProperties.allowedOriginPatterns().isEmpty()
            ? java.util.List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://[::1]:*")
            : corsProperties.allowedOriginPatterns();
    configuration.setAllowedOriginPatterns(patterns);
    configuration.setAllowedMethods(
        java.util.List.of("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(java.util.List.of("*"));
    configuration.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtFilter, SecurityProperties securityProperties)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable);
    http.cors(Customizer.withDefaults());

    if (!securityProperties.isEnabled()) {
      http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
      return http.build();
    }

    http.sessionManagement(
        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.authorizeHttpRequests(
        auth ->
            auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/logout")
                .permitAll()
                .requestMatchers("/api/health", "/actuator/**")
                .permitAll()
                .requestMatchers("/api/hello", "/api/missions/test", "/api/events/**")
                .permitAll()
                .anyRequest()
                .authenticated());
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    http.exceptionHandling(
        ex ->
            ex.authenticationEntryPoint(this::writeUnauthorized)
                .accessDeniedHandler(
                    (request, response, accessDeniedException) ->
                        writeJson(
                            response,
                            HttpServletResponse.SC_FORBIDDEN,
                            accessDeniedException.getMessage())));
    return http.build();
  }

  private void writeUnauthorized(
      jakarta.servlet.http.HttpServletRequest request,
      HttpServletResponse response,
      org.springframework.security.core.AuthenticationException authException)
      throws IOException {
    writeJson(
        response,
        HttpServletResponse.SC_UNAUTHORIZED,
        "Não autenticado. Envie um JWT válido em Authorization: Bearer …");
  }

  private void writeJson(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    Map<String, Object> body = new HashMap<>();
    body.put("ok", false);
    body.put("message", message);
    body.put("timestamp", Instant.now().toString());
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
