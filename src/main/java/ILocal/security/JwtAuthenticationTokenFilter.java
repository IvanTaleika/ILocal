package ILocal.security;

import ILocal.entity.User;
import ILocal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class JwtAuthenticationTokenFilter extends AbstractAuthenticationProcessingFilter {

	@Value("\"${jwt.name.access}\"")
	private String access;

	@Value("\"${jwt.name.refresh}\"")
	private String refresh;

	private final static long timeForAccess = 1_800_000L;
	private final static long timeForRefresh = 1_296_000_000L;

	@Autowired
	private JwtValidator jwtValidator;

	@Autowired
	private JwtGenerator jwtGenerator;

	@Autowired
	private UserRepository userRepository;

	public JwtAuthenticationTokenFilter() {
		super("/lang/**");
		setRequiresAuthenticationRequestMatcher(new OrRequestMatcher(
				new AntPathRequestMatcher("/projects/**"),
				new AntPathRequestMatcher("/lang/**"),
				new AntPathRequestMatcher("/project-lang/**"),
				new AntPathRequestMatcher("/contributors/**"),
				new AntPathRequestMatcher("/terms/**"),
				new AntPathRequestMatcher("/term-lang/**"),
				new AntPathRequestMatcher("/user/**"),
				new AntPathRequestMatcher("/search/**"),
				new AntPathRequestMatcher("/statistic/**")
		));
	}


	@Override
	public Authentication attemptAuthentication(HttpServletRequest httpServletRequest,
												HttpServletResponse httpServletResponse)
			throws AuthenticationException, IOException {

		long now = new Date().getTime();
		String accessHeader = httpServletRequest.getHeader("Auth");
		String refreshHeader = httpServletRequest.getHeader("AuthRef");
		if (accessHeader == null || !accessHeader.startsWith("Token ") || refreshHeader == null || !refreshHeader.startsWith("Refresh ")) {
			httpServletResponse.sendError(420, "UNAUTHORIZED");
			// throw new RuntimeException("JWT Token is missing");
			return null;
		}
		String accessToken = accessHeader.substring(6);
		String refreshToken = refreshHeader.substring(8);

		JwtUser accessUser = jwtValidator.validateAccess(accessToken);
		JwtUser refreshUser = jwtValidator.validateRefresh(refreshToken);

		if (accessUser == null || refreshUser == null) {
			httpServletResponse.sendError(420, "UNAUTHORIZED");
			return null;
		}

		User existUser = userRepository.findByUsername(refreshUser.getUserName());
		if (!existUser.getRefreshToken().equals(refreshToken)) {
			httpServletResponse.sendError(420, "UNAUTHORIZED");
			return null;
		}
		if ((now - refreshUser.getDate().getTime()) > timeForRefresh) {
			String newRefreshToken = jwtGenerator.generateRefresh(refreshUser);
			existUser.setRefreshToken(newRefreshToken);
			userRepository.save(existUser);
			httpServletResponse.sendError(420, "UNAUTHORIZED");
			return null;
		}
		if ((now - accessUser.getDate().getTime()) > timeForAccess) {
			String newRefreshToken = jwtGenerator.generateRefresh(refreshUser);
			existUser.setRefreshToken(newRefreshToken);
			userRepository.save(existUser);
			httpServletResponse.sendError(401, "UNAUTHORIZED");
			return null;
		}
		JwtAuthenticationToken token = new JwtAuthenticationToken(accessToken);
		SecurityContextHolder.getContext()
				.setAuthentication(token);
		return getAuthenticationManager().authenticate(token);
	}


	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);
		chain.doFilter(request, response);
	}
}
