package br.edu.central.centrallj.security;

import br.edu.central.centrallj.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER = "Bearer ";

  private final JwtService jwtService;
  private final SecurityProperties securityProperties;

  public JwtAuthenticationFilter(JwtService jwtService, SecurityProperties securityProperties) {
    this.jwtService = jwtService;
    this.securityProperties = securityProperties;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    if (!securityProperties.isEnabled()) {
      return true;
    }
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    String uri = request.getRequestURI();
    if ("POST".equalsIgnoreCase(request.getMethod())
        && (uri.startsWith("/api/auth/login") || uri.startsWith("/api/auth/logout"))) {
      return true;
    }
    if (uri.startsWith("/api/health")
        || uri.startsWith("/api/hello")
        || uri.startsWith("/api/missions/test")
        || uri.startsWith("/api/events/")
        || uri.startsWith("/actuator/")) {
      return true;
    }
    return false;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    if (!securityProperties.isEnabled()) {
      filterChain.doFilter(request, response);
      return;
    }
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith(BEARER)) {
      String token = header.substring(BEARER.length()).trim();
      UsuarioPrincipal principal = jwtService.parseToken(token);
      if (principal != null) {
        var auth =
            new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    }
    filterChain.doFilter(request, response);
  }
}
