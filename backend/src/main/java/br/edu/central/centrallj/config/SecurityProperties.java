package br.edu.central.centrallj.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "central-lj.security")
public class SecurityProperties {

  private boolean enabled = true;

  private Jwt jwt = new Jwt();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Jwt getJwt() {
    return jwt;
  }

  public void setJwt(Jwt jwt) {
    this.jwt = jwt;
  }

  public static class Jwt {
    /** Segredo HS256 — use variável de ambiente em produção (≥ 32 caracteres). */
    private String secret = "central-lj-dev-secret-min-32b-long-key!!!";

    private long expirationHours = 48;

    public String getSecret() {
      return secret;
    }

    public void setSecret(String secret) {
      this.secret = secret;
    }

    public long getExpirationHours() {
      return expirationHours;
    }

    public void setExpirationHours(long expirationHours) {
      this.expirationHours = expirationHours;
    }
  }
}
