package ILocal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtGenerator {

    @Value("\"${jwt.secret.access}\"")
    private String access;

    @Value("\"${jwt.secret.refresh}\"")
    private String refresh;

    public String generateAccess(JwtUser jwtUser) {
        return generate(jwtUser, access);
    }

    public String generateRefresh(JwtUser jwtUser) {
        return generate(jwtUser, refresh);
    }

    private String generate(JwtUser jwtUser, String secret) {
        Claims claims = Jwts.claims().setSubject(jwtUser.getUserName());
        claims.put("userId", String.valueOf(jwtUser.getId()));
        claims.put("date", new Date().getTime());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }
}
