package br.edu.central.centrallj;

import br.edu.central.centrallj.config.CorsProperties;
import br.edu.central.centrallj.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableConfigurationProperties({CorsProperties.class, SecurityProperties.class})
@EnableKafka
public class CentralLjApplication {
  public static void main(String[] args) {
    SpringApplication.run(CentralLjApplication.class, args);
  }
}

