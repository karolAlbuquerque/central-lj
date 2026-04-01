package br.edu.central.centrallj.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

  private final List<String> allowedOriginPatterns;

  public CorsConfig(CorsProperties corsProperties) {
    List<String> patterns = corsProperties.allowedOriginPatterns();
    this.allowedOriginPatterns =
        patterns == null || patterns.isEmpty()
            ? List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://[::1]:*")
            : patterns;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOriginPatterns(allowedOriginPatterns.toArray(String[]::new))
        .allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .maxAge(3600);
  }
}
