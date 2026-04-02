package br.edu.central.centrallj.security;

import br.edu.central.centrallj.config.SecurityProperties;
import br.edu.central.centrallj.domain.Usuario;
import br.edu.central.centrallj.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

  private final SecretKey key;
  private final long expirationMs;
  private static final String CLAIM_NOME = "nome";
  private static final String CLAIM_EMAIL = "email";
  private static final String CLAIM_ROLE = "role";
  private static final String CLAIM_HEROI = "heroiId";

  public JwtService(SecurityProperties securityProperties) {
    String secret = securityProperties.getJwt().getSecret();
    if (secret == null || secret.length() < 32) {
      throw new IllegalStateException(
          "central-lj.security.jwt.secret deve ter pelo menos 32 caracteres.");
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = securityProperties.getJwt().getExpirationHours() * 3_600_000L;
  }

  public String createToken(Usuario usuario) {
    Instant now = Instant.now();
    Instant exp = now.plusMillis(expirationMs);
    UUID heroiId = usuario.getHeroi() != null ? usuario.getHeroi().getId() : null;
    var builder =
        Jwts.builder()
            .subject(usuario.getId().toString())
            .claim(CLAIM_EMAIL, usuario.getEmail())
            .claim(CLAIM_NOME, usuario.getNome())
            .claim(CLAIM_ROLE, usuario.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(key);
    if (heroiId != null) {
      builder.claim(CLAIM_HEROI, heroiId.toString());
    }
    return builder.compact();
  }

  public UsuarioPrincipal parseToken(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }
    try {
      Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
      UUID id = UUID.fromString(claims.getSubject());
      String email = claims.get(CLAIM_EMAIL, String.class);
      String nome = claims.get(CLAIM_NOME, String.class);
      UserRole role = UserRole.valueOf(claims.get(CLAIM_ROLE, String.class));
      String hid = claims.get(CLAIM_HEROI, String.class);
      UUID heroiUuid = hid != null && !hid.isBlank() ? UUID.fromString(hid) : null;
      return new UsuarioPrincipal(id, email, nome, role, heroiUuid);
    } catch (Exception e) {
      return null;
    }
  }
}
