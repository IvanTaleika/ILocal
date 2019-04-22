package ILocal.security;

import ILocal.entity.JwtAuthenticationToken;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

public class JwtAuthenticationTokenFilter extends AbstractAuthenticationProcessingFilter {


    public JwtAuthenticationTokenFilter() {
        super("/lang/**");
        setRequiresAuthenticationRequestMatcher(new OrRequestMatcher(
                new AntPathRequestMatcher("/projects/**"),
                new AntPathRequestMatcher("/lang/**"),
                new AntPathRequestMatcher("/project-lang/**"),
                new AntPathRequestMatcher("/contributors/**"),
                new AntPathRequestMatcher("/terms/**"),
                new AntPathRequestMatcher("/term-lang/**"),
                new AntPathRequestMatcher("/user/**")
        ));
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest,
                                                HttpServletResponse httpServletResponse)
            throws AuthenticationException, IOException {
        String header = httpServletRequest.getHeader("Authorization");
        if (header == null || !header.startsWith("Token ")) {
            httpServletResponse.sendError(401, "UNAUTHORIZED");
           // throw new RuntimeException("JWT Token is missing");
            return null;
        }
        String authenticationToken = header.substring(6);
        JwtAuthenticationToken token = new JwtAuthenticationToken(authenticationToken);
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
