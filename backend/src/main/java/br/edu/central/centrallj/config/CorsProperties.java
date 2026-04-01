package br.edu.central.centrallj.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "central-lj.cors")
public record CorsProperties(List<String> allowedOriginPatterns) {}
