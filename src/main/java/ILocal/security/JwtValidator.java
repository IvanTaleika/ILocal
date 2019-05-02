package ILocal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtValidator {

	@Value("\"${jwt.secret.access}\"")
	private String access;

	@Value("\"${jwt.secret.refresh}\"")
	private String refresh;

	public JwtUser validateAccess(String token) {
		return validate(token, access);
	}

	public JwtUser validateRefresh(String token) {
		return validate(token, refresh);
	}

	private JwtUser validate(String token, String secret) {
		JwtUser jwtUser = null;
		try {
			Claims body = Jwts.parser()
					.setSigningKey(secret)
					.parseClaimsJws(token)
					.getBody();
			jwtUser = new JwtUser(Long.parseLong((String) body.get("userId")), body.getSubject(), (long) body.get("date"));
		} catch (Exception e) {
			System.out.printf("Error jwt = %s%n", e);
		}
		return jwtUser;
	}
}
