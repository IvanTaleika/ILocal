package ILocal.security;

import ILocal.entity.JwtUser;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

@Component
public class JwtGenerator {

  public String generate(JwtUser jwtUser) {

    Claims claims = Jwts.claims().setSubject(jwtUser.getUserName());
    claims.put("userId", String.valueOf(jwtUser.getId()));
    claims.put("role", jwtUser.getRole());
    return Jwts.builder()
        .setClaims(claims)
        .signWith(SignatureAlgorithm.HS512, "secret").compact();
  }
}
