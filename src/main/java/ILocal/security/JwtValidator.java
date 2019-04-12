package ILocal.security;

import ILocal.entity.JwtUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtValidator {
    private String secret = "secret";
    public JwtUser validate(String token) {
        JwtUser jwtUser = null;
        try {
            Claims body = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            jwtUser = new JwtUser(Long.parseLong((String) body.get("userId")),body.getSubject());

        }
        catch (Exception e) {
            System.out.println(e);
        }
        return jwtUser;
    }
}
